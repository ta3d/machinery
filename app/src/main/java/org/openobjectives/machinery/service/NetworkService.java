/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jraska.console.Console;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.openobjectives.machinery.A3MenuResourcesActivity;
import org.openobjectives.machinery.R;
import org.openobjectives.machinery.model.ResourceHelper;
import org.openobjectives.machinery.model.Server;
import org.openobjectives.machinery.helper.StringCrypter;
import org.openobjectives.machinery.model.Unit;
import org.openobjectives.machinery.model.Command;
import org.openobjectives.machinery.tasks.CrudNetworkTask;
import org.openobjectives.machinery.tasks.ListNetworkTask;
import org.openobjectives.machinery.utils.DbHelper;
import org.openobjectives.machinery.utils.GeneralHelper;
import org.openobjectives.machinery.utils.HighlightWordsChecker;
import org.openobjectives.machinery.utils.LocalDataStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import me.snowdrop.istio.client.DefaultIstioClient;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
/**
 * <B>Class: NetworkService </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The service managing ssh/https connections to the cluster <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class NetworkService extends Service {

    private static final String TAG = NetworkService.class.getSimpleName();
    private final IBinder mBinder = new NetworkBinder();
    private DbHelper db;
    private Unit actualUnit;
    private int unitId=0;
    private Handler handler=null;
    private Runnable runnable;
    private final int runTime = 15000;
    private boolean isRunning = true;
    private boolean threadRunning=false;
    private Connection conn=null;
    private Session sess=null;
    public boolean serverOnline;
    public boolean viaJumphost;
    public boolean viaKubeconfig;
    private int tunnelport = 0;
    private Thread connThread;
    private Server apiserver=null;
    private String apiserverHost=null;
    private int apiserverPort = 0;
    private String kubecfg = null;
    private KubernetesClient client =null;
    private DefaultIstioClient defaultIstioClient=null;
    private String namespace=null;
    private InputStream podlogInputStream=null;
    private Console logConsole=null;
    private Thread podlogThread=null;
    private Config config;
    private io.fabric8.kubernetes.api.model.Config modelConfig;
    private ArrayList<HasMetadata> actualList = null;
    private Collection<String> highlightWords;
    private HighlightWordsChecker highlightWordsChecker;
    public String actualErrorMessage="";

    private void initDB() {
        db = new DbHelper(getApplicationContext());
        actualUnit = db.findUnitById(unitId);
        highlightWords = Arrays.asList(db.getPrefString("hintInLogFor").split(","));
        highlightWordsChecker = new HighlightWordsChecker(highlightWords);
        apiserver = actualUnit
                .getServers()
                .stream()
                .filter(item -> item.getName().equals(LocalDataStore.APISERVER))
                .findAny()
                .orElse(null);
        if (apiserver == null) {
            Server kubeconfigServer = actualUnit
                    .getServers()
                    .stream()
                    .filter(item -> item.getName().equals(LocalDataStore.KUBECONFIG))
                    .findAny()
                    .orElse(null);
            SharedPreferences settings = getSharedPreferences("secret", 0);
            String key = settings.getString("key", "");
            String iv = settings.getString("iv", "");
            try {

                kubecfg = StringCrypter.decrypt(kubeconfigServer.getKey(), key, iv);
            } catch (Exception e) {
                //Log.e(TAG+"-initDB", "Loading encrypted kubeconfig went wrong -" + e.getStackTrace());
                throw new RuntimeException("Loading encryptedkubeconfig went wrong -" + e.getStackTrace());
            }
            viaKubeconfig = true;
            try {
                config = Config.fromKubeconfig(kubecfg);
                modelConfig = KubeConfigUtils.parseConfigFromString(kubecfg);
                URL url = new URL(config.getMasterUrl());
                //config. Workaround as authinfo not loaded
//                List<NamedAuthInfo> aiList = modelConfig.getUsers();
//                aiList.stream()
//                .filter(item -> item.getName().equals(config.getRequestConfig().getUsername()))
//                .findAny()
//                .orElse(null);

                apiserverHost = url.getHost();
                apiserverPort = url.getPort();
                if (apiserverPort < 1)
                    apiserverPort = config.getMasterUrl().startsWith("https") ? 443 : 80;
                apiserver = new Server(apiserverHost, apiserverPort, "tmpserver", config.getRequestConfig().getUsername(), config.getRequestConfig().getPassword(), "");
            } catch (IOException e) {
                //Log.e(TAG+"-initKubernetesClient", "Loading kubeconfig went wrong (CHECK YAML) -" + e.getStackTrace());
                throw new RuntimeException("Loading kubeconfig went wrong (CHECK YAML) -" + e.getStackTrace());
            }

        } else {
            try {
                URL url = new URL(apiserver.getHostname());
                apiserverHost = url.getHost();
                apiserverPort = apiserver.getPort();
                if (apiserverPort < 1)
                    apiserverPort = apiserver.getHostname().startsWith("https") ? 443 : 80;
            } catch (MalformedURLException e) {
                //Log.e(TAG, "MalformedURLException");
            }
        }
    }
    private boolean isWithJumphost() {
        Server jump = actualUnit
                .getServers()
                .stream()
                .filter(item -> item.getName().equals(LocalDataStore.SSHSERVER))
                .findAny()
                .orElse(null);
        return jump==null?false:true;
    }

    private final X509TrustManager x509TrustManager= new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    };
    private final TrustManager[] trustAllCerts = new TrustManager[]{
            x509TrustManager
    };



    private void initKubernetesClient() {
        String password = null;
        SharedPreferences settings = getSharedPreferences("secret", 0);
        String key = settings.getString("key", "");
        String iv = settings.getString("iv", "");
        if (viaKubeconfig) {
            //we added before the pass unencrypted
            //TODO: add pw also encrypted before
            password = apiserver.getPassword();
        } else {
            String cryptedPass = apiserver.getPassword();
            String cryptedKey = apiserver.getKey();
            if (cryptedPass != null && !cryptedPass.equals("")) {
                try {
                    password = StringCrypter.decrypt(cryptedPass, key, iv);
                } catch (Exception e) {
                    //Log.e(TAG+"-initKubernetesClient", "Decrypt of Password went wrong");
                    throw new RuntimeException("Decryption of Password went wrong");
                }
            }
        }
        final String finalpassword = password;
        if (viaKubeconfig) {
            if (viaJumphost) {
                int randomNum = ThreadLocalRandom.current().nextInt(10, 98);
                tunnelport = 65000 + randomNum;
                String protPrefix = config.getMasterUrl().startsWith("https://") ? "https://" : "http://";
                config.setMasterUrl(protPrefix + "localhost:" + tunnelport);
            }
        } else if (!viaKubeconfig && viaJumphost) {
            int randomNum = ThreadLocalRandom.current().nextInt(10, 98 );
            tunnelport = 65000+randomNum;
            String protPrefix = apiserver.getHostname().startsWith("https://")?"https://":"http://";
            config = new ConfigBuilder()
                    .withMasterUrl(protPrefix+"localhost:"+tunnelport)
                    .withUsername(apiserver.getUser())
                    .withPassword(finalpassword)
                    .build();
        }else{
            config = new ConfigBuilder()
                    .withMasterUrl(apiserver.getHostname()+":"+apiserver.getPort())
                    .withUsername(apiserver.getUser())
                    .withPassword(finalpassword)
                    .build();
        }
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            if (db.getPrefBool("allowSelfsigned")) {
                OkHttpClient httpClient = new OkHttpClient()
                        .newBuilder()
                        .sslSocketFactory(sslSocketFactory, x509TrustManager)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String s, SSLSession sslSession) {
                                return true;
                            }
                        })
                        .authenticator(new Authenticator() {
                            @Override
                            public Request authenticate(Route route, Response response) throws IOException {
                                if (finalpassword != null) {
                                    String credential = Credentials.basic(apiserver.getUser(), finalpassword);
                                    return response.request().newBuilder().header("Authorization", credential).build();
                                } else {
                                    return response.request();
                                }
                            }
                        })
                        .build();
                client = new DefaultKubernetesClient(httpClient, config);
                defaultIstioClient = new DefaultIstioClient(httpClient, config);
            }
            else {
                client = new DefaultKubernetesClient(config);
                defaultIstioClient = new DefaultIstioClient(config);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG,"Show SSL not valid Cert dialog");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        unitId = intent.getIntExtra("mUnitId", 0);
        namespace = intent.getStringExtra("mNamespace");
        super.onStartCommand(intent, flags, startId);
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_LOW);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setSound(null)
                    .setSmallIcon(R.drawable.oo_hexagon_slice_6)
                    .setContentText("").build();

            startForeground(1, notification);
        }
        initDB();
        viaJumphost = isWithJumphost();
        if(handler==null) {
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    boolean isPaused = db.getPrefBool("isPaused");
                    //the 3 states - this is mesurement mode
                    if (isRunning && !isPaused) {
                        //Log.i(TAG,"measuring");
                        try {
                            if(client==null)
                                initKubernetesClient();
                            if(namespace==null)
                                namespace="any";
                            if(viaJumphost)
                                connThread = new Thread(connSshThread);
                            else
                                connThread = new Thread(connHttpThread);
                            connThread.start();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        handler.postDelayed(runnable, runTime);
                    }
                    //this is paused
                    else if (isRunning && isPaused) {

                        //Log.i(TAG,"paused");
                        handler.postDelayed(runnable, runTime);
                    }
                    //this is terminating
                    else {

                        //Log.i(TAG,"ready for destroy");
                    }
                }
            };
            handler.post(runnable);
        }
        return Service.START_STICKY;
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //SOMEHOW THIS IS NOT REACHED
        unitId = intent.getExtras().getInt("mUnitId");

    }

    @Override
    public void onDestroy() {

        //Toast.makeText(this, "Network Service Stopped", Toast.LENGTH_LONG).show();
        isRunning=false;
        //close logging stuff
        logConsole=null;
        try {
            if(podlogInputStream!=null)
                podlogInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    public ArrayList<HasMetadata> getResources(String resource, String resourceIdent){
        try {
            //Log.i(TAG,""+client.getMasterUrl());
            //TODO set bak to 20sec
            actualList = new ListNetworkTask(this, client, defaultIstioClient).execute(resource,LocalDataStore.CMD_LIST, namespace, resourceIdent).get(200, TimeUnit.SECONDS);
            return actualList;
       } catch (Exception e) {
            return null;

        }
    }

    public Boolean deleteResource(String resource, String name, String namespace) {
        try {
            //Log.i(TAG,""+client.getMasterUrl());
            return new CrudNetworkTask(client).execute(resource, LocalDataStore.CMD_DELETE, name, namespace).get(10, TimeUnit.SECONDS);
       } catch (Exception e) {
            actualErrorMessage=e.getMessage();
            return null;
        }
    }

    public boolean tailPodLog(String inNamespace, String podname, NestedScrollView nestedScrollView, MutableBoolean dontscroll, int sinceTime, String containerName){
        podlogInputStream=null;
        stopPodlogThread();
        Console.clear();
        //Pod result = client.pods().mInNamespace(mNamespace).withName(mPodname).get();
        //TODO FIX any = all
        //mNamespace = mNamespace.equals("any") ? "default" : mNamespace;
        LogWatch logWatch =null;
        if(sinceTime==0)
            logWatch = client.pods().inNamespace(inNamespace).withName(podname).inContainer(containerName).tailingLines(60).watchLog();
        else
            logWatch = client.pods().inNamespace(inNamespace).withName(podname).inContainer(containerName).usingTimestamps().sinceSeconds(sinceTime*60).watchLog();
        podlogInputStream = logWatch.getOutput();
        try {
            podlogThread = new Thread() {
                @Override
                public void run() {

                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(podlogInputStream));
                        while (true) {
                            String line = reader.readLine();
                            if (line == null) {
                                //return;
                                //Thread.sleep(1000);
                                continue;
                            }

                            if (highlightWordsChecker.containsHighWords(line)) {
                                SpannableString spannableString = new SpannableString(line);
                                ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(getResources().getColor(R.color.colorAccent));
                                spannableString.setSpan(foregroundSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                Console.writeLine(spannableString);
                            }
                            else{
                                Console.writeLine(line);
                            }
                            if(!dontscroll.isTrue()) {
                                nestedScrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        nestedScrollView.fullScroll(View.FOCUS_DOWN);
                                    }
                                });
                            }
                            //mNestedScrollView.fullScroll(View.FOCUS_DOWN);
                            //Log.i(TAG, line);
                        }
                    } catch (IOException e) {
                        // Check again the latch which could be already count down to zero in between
                        // so that an IO exception occurs on read
                        //if (terminateLatch.getCount() > 0L) {
                        //Log.e(TAG, "IO Log Error", e);
                        //}
                    }
                }
            };
            //return new PodLogTask(client,mPodname).execute("test").get(5, TimeUnit.SECONDS);
            podlogThread.start();
            return true;
        } catch (Exception e) {
            actualErrorMessage=e.getMessage();
            return false;
        }
    }

    public void stopPodlogThread(){
        if(podlogThread!=null){
            podlogThread.interrupt();
            podlogThread=null;
        }
    }

    public boolean describeResource(String namespace, String resourcename, NestedScrollView nestedScrollView, String resourcetype, String concreteResource) {
        stopPodlogThread();
        if (actualList == null)
            getResources(resourcetype, null);
        HasMetadata object = actualList.stream()
                .filter(item -> item.getMetadata().getName().equals(resourcename))
                .findAny()
                .orElse(null);
        if (resourcetype.equals(LocalDataStore.RES_DEPLOYMENT)) {
            Deployment d = (Deployment) object;
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String yaml = null;
            try {
                yaml = mapper.writeValueAsString(d);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            yaml = GeneralHelper.santinzeYaml(yaml);
            Console.clear();
            Console.writeLine(yaml);
        }else if (resourcetype.equals(LocalDataStore.RES_SERVICE)) {
            io.fabric8.kubernetes.api.model.Service s = (io.fabric8.kubernetes.api.model.Service) object;
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String yaml = null;
            try {
                yaml = mapper.writeValueAsString(s);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            yaml = GeneralHelper.santinzeYaml(yaml);
            Console.clear();
            Console.writeLine(yaml);
        }else if (resourcetype.equals(LocalDataStore.RES_POD)) {
            io.fabric8.kubernetes.api.model.Pod s = (io.fabric8.kubernetes.api.model.Pod) object;
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String yaml = null;
            try {
                yaml = mapper.writeValueAsString(s);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            yaml = GeneralHelper.santinzeYaml(yaml);
            Console.clear();
            Console.writeLine(yaml);
        }else if (resourcetype.equals(LocalDataStore.RES_RESOURCES)) {
            HasMetadata actual=null;
            ResourceHelper helper = new ResourceHelper();
            actual = helper.getConcreteResourceItem(actualList,resourcetype,concreteResource);
            if(actual != null) {
                //io.fabric8.kubernetes.api.model.Pod s = (io.fabric8.kubernetes.api.model.Pod) object;
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                String yaml = null;
                try {
                    yaml = mapper.writeValueAsString(actual);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                Console.clear();
                yaml = GeneralHelper.santinzeYaml(yaml);
                Console.writeLine(yaml);
            }else{
                Toast.makeText(this, " NOT reached - No Resources for "+resourcename, Toast.LENGTH_LONG).show();
                Intent intent = null;
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(NetworkService.this, A3MenuResourcesActivity.class
                        .getName());
                intent.putExtra("mNamespace", namespace);
                intent.putExtra("mResource", LocalDataStore.RES_RESOURCES);
                intent.putExtra("mUnitId", unitId);
                intent.putExtra("mResourceIdent", resourcename);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
        return true;
    }



    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        stopPodlogThread();
        return mBinder;
    }


    BroadcastReceiver stateListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Incoming State ", Toast.LENGTH_SHORT).show();
        }
    };

    public class NetworkBinder extends Binder {
        public NetworkService getService() {
            return NetworkService.this;
        }
    }



    Thread connHttpThread = new Thread(new Runnable() {
        @Override
        public void run() {
            if (actualUnit == null)
                initDB();
            if (threadRunning) {
                //Log.i(TAG,"Another Thread checks.. delay");
            } else {
                threadRunning = true;
                serverOnline = pingHost(apiserver);
                //Log.i(TAG,apiserver.getHostname() + "reachable " + serverOnline);
                serverOnline = true;
                threadRunning=false;
            }
        }

        public boolean pingHost(Server apiserver) {
            //TODO check to deactivate
            if (apiserver.getHostname().startsWith("https")){
//                  Matcher m =Pattern.compile("^(http|https)?://.*?").matcher(apiserver.getHostname());
//                  String result = m.matches() ? m.group(0) : apiserver.getHostname();
                  try {
	                new URL(apiserver.getHostname()+":"+apiserver.getPort()).openConnection();
                    return true;
                  } catch (Exception e) {
                    return false; // Either timeout or unreachable or failed DNS lookup.
                }
            }else if (apiserver.getHostname().startsWith("http")){
                  try {
                      Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(apiserverHost, apiserver.getPort()), 10000);
                    socket.close();
                    return true;
                  } catch (Exception e) {
                    return false; // Either timeout or unreachable or failed DNS lookup.
                }
            }else{
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(apiserver.getHostname(), apiserver.getPort()), 10000);
                    socket.close();
                    return true;
                } catch (Exception e) {
                    return false; // Either timeout or unreachable or failed DNS lookup.
                }
            }
        }
    }
    );


    Thread connSshThread = new Thread(new Runnable(){
    @Override
    public void run() {
        if(actualUnit==null)
            initDB();
        if (threadRunning){
            //Log.i(TAG,"Another Thread checks.. delay");
        }
        else if(conn==null){
            threadRunning=true;
            String res = createConnection(actualUnit.getCommands().get(0));
            //Log.i(TAG,res);
        } else {
            threadRunning=true;
            String res = execCommand(actualUnit.getCommands().get(0));
            //Log.i(TAG,res);
        }
    }

    public String execCommand(Command c) {
            try {
                conn.ping();
                serverOnline = true;
                threadRunning=false;
                return "Connection OK";
            } catch (IOException e) {
                serverOnline = false;
                conn=null;
                threadRunning=false;
                return "Connection Error: " + e.getMessage();

            } catch (Exception e) {
                e.printStackTrace();
                serverOnline = false;
                conn=null;
                threadRunning=false;
                return "other error" + e.getMessage();
            }
        }
        public String createConnection(Command c) {
            Server server = c.getServer();
            String hostname = server.getHostname();
            int port = server.getPort();
            String username = server.getUser();
            String password = server.getPassword();
            String sshkey = server.getKey();
            boolean isKeyAuth = true;
            if (password != null && !password.equals("")) {
                try {
                    SharedPreferences settings = getSharedPreferences("secret", 0);
                    String key = settings.getString("key", "");
                    String iv = settings.getString("iv", "");
                    password = StringCrypter.decrypt(password, key, iv);
                    isKeyAuth = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    //Log.e(TAG+":execCommand", "Decrypt of Password went wrong");
                    throw new RuntimeException("Decryption of Password went wrong");
                }
            } else {
                try {
                    SharedPreferences settings = getSharedPreferences("secret", 0);
                    String key = settings.getString("key", "");
                    String iv = settings.getString("iv", "");
                    sshkey = StringCrypter.decrypt(sshkey, key, iv);
                    isKeyAuth = true;
                } catch (Exception e) {
                    //Log.e(TAG+":execCommand", "Decrypt of SSHKey went wrong");
                    throw new RuntimeException("Decryption of SSHKey went wrong");
                }
            }
            String status;
            try {
                /* Create a connection instance */
                conn = new Connection(hostname, port);
                conn.setTCPNoDelay(true);
                /* Now connect */
                conn.connect(null, 15 * 1000, 15 * 1000);
                /*
                 * Authenticate. If you get an IOException saying something like
                 * "Authentication method password not supported by the server at this stage."
                 * then please check the FAQ.
                 */

                //to ensure linefeeds inbetween key tags (which trilead sshlibary needs)
                //we have to reinsert them as base64encode removed them
                if (sshkey != null)
                    sshkey = GeneralHelper.addLineFeedsToSshKey(sshkey);
                boolean isAuthenticated = isKeyAuth ? conn.authenticateWithPublicKey(username, sshkey.toCharArray(), null) :
                        conn.authenticateWithPassword(username, password);

                if (isAuthenticated == false)
                    throw new IOException("Authentication failed for " + username
                            + "@" + hostname + ":" + port);
                conn.createLocalPortForwarder(tunnelport, apiserverHost, apiserverPort);
                /* Create a session */
                sess = conn.openSession();
                sess.execCommand(c.getCommand());
                /*
                 * This basic example does not handle stderr, which is sometimes
                 * dangerous (please read the FAQ).
                 */
                InputStream stdout = new StreamGobbler(sess.getStdout());
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(stdout));
                String txt = "";
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    txt += line + "\n";
                }

                if (txt == null || txt.trim().equals(""))
                    txt = c.getName() + " executed!";
                status = txt;
                /* Show exit status, if available (otherwise "null") */
                // Log.e(TAG,"ExitCode: " + sess.getExitStatus());
                /* Close this session */
                sess.close();
                /* Close the connection */
                //conn.close();
                serverOnline = true;
                threadRunning = false;
                return status;
            } catch (IOException e) {
                serverOnline = false;
                conn = null;
                threadRunning = false;
                return "Connection Error: " + e.getMessage();

            } catch (Exception e) {
                e.printStackTrace();
                serverOnline = false;
                conn = null;
                threadRunning = false;
                return "other error" + e.getMessage();
            }

        }

    });

}
/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.jraska.console.Console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
/**
 * <B>Class: PodLogTask </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Follow the podlog <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class PodLogTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = PodLogTask.class.getSimpleName();
    private Exception exception;
    private KubernetesClient client = null;
    private String podname = null;
    private Object IOException;

    public PodLogTask(KubernetesClient client, String podname) {
        this.podname = podname;
        this.client = client;
    }

    protected Boolean doInBackground(String... urls) {
        try {
            LogWatch logWatch = client.pods().withName(podname).watchLog();
            InputStream podlogInputStream = logWatch.getOutput();
            BufferedReader reader = new BufferedReader(new InputStreamReader(podlogInputStream));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    ;
                    continue;
                }
                Console.writeLine(line);
                //Log.i(TAG, line);
            }
        } catch (IOException e) {
            this.exception = e;
            return false;
        }
    }


    protected void onPostExecute(String myparam) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

}

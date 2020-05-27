/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.openobjectives.machinery.model.ResourceHelper;
import org.openobjectives.machinery.service.NetworkService;
import org.openobjectives.machinery.utils.LocalDataStore;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import me.snowdrop.istio.client.DefaultIstioClient;

/**
 * <B>Class: ListNetworkTask </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: List the resources <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class ListNetworkTask extends AsyncTask<String, Void, ArrayList<HasMetadata>> {

    private static final String TAG = ListNetworkTask.class.getSimpleName();
    private Exception exception;
    private KubernetesClient client = null;
    private DefaultIstioClient defaultIstioClient = null;
    private NetworkService service;

    public ListNetworkTask(NetworkService service, KubernetesClient client, DefaultIstioClient defaultIstioClient) {
        this.service = service;
        this.client = client;
        this.defaultIstioClient = defaultIstioClient;
    }

    protected ArrayList<HasMetadata> doInBackground(String... urls) {
        try {
            if (urls[1].equals(LocalDataStore.CMD_LIST)) {
                switch (urls[0]) {
                    case LocalDataStore.RES_POD:
                        return getPods(urls[2]);
                    case LocalDataStore.RES_SERVICE:
                        return getServices(urls[2]);
                    case LocalDataStore.RES_DEPLOYMENT:
                        return getDeployments(urls[2]);
                    case LocalDataStore.RES_NAMESPACE:
                        return getNamespaces();
                    case LocalDataStore.RES_RESOURCES:
                        return getOtherResources(urls[3]);
                    default:
                        return null;
                }
            }
        } catch (Exception e) {
            this.exception = e;
            e.printStackTrace();
            //service.actualErrorMessage=e.getMessage();
            return null;
        }
        return null;
    }


    protected void onPostExecute(String myparam) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

    protected ArrayList<HasMetadata> getPods(String namespace) {
        PodList podlist = null;
        ArrayList<HasMetadata> res = null;
        try {
            if (namespace.equals("any"))
                podlist = client.pods().inAnyNamespace().list();
            else
                podlist = client.pods().inNamespace(namespace).list();
//            ArrayList<HasMetadata> res = podlist.getItems().stream()
//                .filter(item -> item!=null)
//                .map(p -> new Boolean(false))
//                .collect(Collectors.toList());
            res = new ArrayList<>(podlist.getItems());
        } catch (Exception e) {
            service.actualErrorMessage=e.getCause().getMessage();
            e.printStackTrace();
        }
        return res;
    }

    protected ArrayList<HasMetadata> getServices(String namespace) {
        ServiceList slist = null;
        if (namespace.equals("any"))
            slist = client.services().inAnyNamespace().list();
        else
            slist = client.services().inNamespace(namespace).list();
        ArrayList<HasMetadata> res = new ArrayList<>(slist.getItems());
        return res;
    }

    protected ArrayList<HasMetadata> getDeployments(String namespace) {
        DeploymentList dlist = null;
        if (namespace.equals("any"))
            dlist = client.extensions().deployments().inAnyNamespace().list();
        else
            dlist = client.extensions().deployments().inNamespace(namespace).list();
        ArrayList<HasMetadata> res = new ArrayList<>(dlist.getItems());
        return res;
    }

    protected ArrayList<HasMetadata> getNamespaces() {
        NamespaceList nlist = null;
        nlist = client.namespaces().list();
        ArrayList<HasMetadata> res = new ArrayList<>(nlist.getItems());
        return res;
    }


    protected ArrayList<HasMetadata> getOtherResources(String resourceIdent) {
        ArrayList<HasMetadata> res = null;
        if (resourceIdent != null && resourceIdent != "") {
//            if(Arrays.stream(ResourceHelper.ISTIO_RESOURES).anyMatch(mResourceIdent::equals)){
//                ResourceHelper resourceHelper = new ResourceHelper();
//                res = resourceHelper.getConcreteResourceItems(client, mResourceIdent);
//            }else{
            ResourceHelper resourceHelper = new ResourceHelper();
            res = resourceHelper.getConcreteResourceItems(client, defaultIstioClient, resourceIdent);
//            }
        } else {
            CustomResourceDefinitionList crds = client.customResourceDefinitions().list();
            List<CustomResourceDefinition> crdsItems = crds.getItems();
            res = new ArrayList<HasMetadata>(crdsItems);
            boolean istioIntsalled = false;
            for (CustomResourceDefinition crd : crdsItems) {
                ObjectMeta metadata = crd.getMetadata();
                if (metadata != null) {
                    String name = metadata.getName();
                    Log.i(TAG,"    " + name + " => " + metadata.getSelfLink());
                    if (name.contains("istio.io")) {
                        istioIntsalled = true;
                        break;
                    }
                }
            }
            ResourceHelper resourceHelper = new ResourceHelper();
            res = resourceHelper.getResourcesList(istioIntsalled);
        }
        return res;
    }

}

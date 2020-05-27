/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.tasks;

import android.os.AsyncTask;

import org.openobjectives.machinery.utils.LocalDataStore;

import java.util.ArrayList;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import me.snowdrop.istio.client.DefaultIstioClient;

/**
 * <B>Class: GetResourceTask </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Load concrete resource in background <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class GetResourceTask extends AsyncTask<String, Void, HasMetadata> {

    private static final String TAG = GetResourceTask.class.getSimpleName();
    private Exception exception;
    private KubernetesClient client = null;

    public GetResourceTask(KubernetesClient client) {
        this.client = client;
    }

    protected HasMetadata doInBackground(String... urls) {
        try {
            if (urls[1].equals(LocalDataStore.CMD_GET)) {
                switch (urls[0]) {
                    case LocalDataStore.RES_RESOURCES:
                        return getConcreteResources(urls[2], urls[3], urls[4]);
                    default:
                        return null;
                }
            }
        } catch (Exception e) {
            this.exception = e;
            e.printStackTrace();
            return null;
        }
        return null;
    }


    protected void onPostExecute(String myparam) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }


    protected HasMetadata getConcreteResources(String namespace, String resourceIdent, String concreteResource) {
        DefaultIstioClient executor = new DefaultIstioClient(client.getConfiguration());
        ArrayList<HasMetadata> res = (ArrayList<HasMetadata>) executor.resourceList(resourceIdent);
        for (HasMetadata concrete : res) {
            if (concrete.getMetadata().getName().equals(concreteResource)) {
                HasMetadata result = executor.resource(resourceIdent).get();
                return result;
            }
        }
        return null;
    }


}

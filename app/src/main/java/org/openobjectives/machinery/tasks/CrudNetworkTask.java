/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.tasks;

import android.os.AsyncTask;

import org.openobjectives.machinery.utils.LocalDataStore;

import io.fabric8.kubernetes.client.KubernetesClient;
/**
 * <B>Class: CrudNetworkTask </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The background CRUD works, currently only delete <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class CrudNetworkTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = CrudNetworkTask.class.getSimpleName();
    private KubernetesClient client = null;
    private Exception exception;

    public CrudNetworkTask(KubernetesClient client) {
        this.client = client;
    }

    protected Boolean doInBackground(String... urls) {

        try {
            if (urls[1].equals(LocalDataStore.CMD_DELETE)) {
                switch (urls[0]) {
                    case LocalDataStore.RES_POD:
                        return deletePod(urls[2], urls[3]);
                    case LocalDataStore.RES_SERVICE:
                        return deleteService(urls[2], urls[3]);
                    case LocalDataStore.RES_DEPLOYMENT:
                        return deleteDeployment(urls[2], urls[3]);
                    default:
                        return false;
                }
            }
        } catch (Exception e) {
            this.exception = e;
            return false;
        }
        return false;
    }


    protected void onPostExecute(String myparam) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }


    protected boolean deletePod(String name, String namespace) {
        return client.pods().inNamespace(namespace).withName(name).delete();
    }

    protected boolean deleteService(String name, String namespace) {
        return client.services().inNamespace(namespace).withName(name).delete();
    }

    protected boolean deleteDeployment(String name, String namespace) {
        return client.extensions().deployments().inNamespace(namespace).withName(name).delete();
    }

}

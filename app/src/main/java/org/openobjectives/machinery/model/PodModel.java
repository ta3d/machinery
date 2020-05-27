/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.model;

import io.fabric8.kubernetes.api.model.Pod;
/**
 * <B>Class: PodModel </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: Extend the pod representation  <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class PodModel extends Pod {

    private static final String TAG = PodModel.class.getSimpleName();
    public boolean swipe = false;

    public PodModel(Pod p) {
        super(p.getApiVersion(), p.getKind(), p.getMetadata(), p.getSpec(), p.getStatus());
    }

}

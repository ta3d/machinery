/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.utils;

import org.openobjectives.machinery.R;

/**
 * <B>Class: LocalDataStore </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The local constants <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class LocalDataStore {

    private static final String TAG = LocalDataStore.class.getSimpleName();
	public static final String STATUSCOMMAND = "uname -a && date && uptime && who";

    public static final int A0_HELP = 0;
    public static final int A0_SETTINGS = 1;
    public static final int A0_IMEXPORT = 2;
    public static final int A0_DELETE_UNIT = 3;

	public static final int TAIL = 0;
	public static final int M1 = 1;
	public static final int M5 = 5;
	public static final int M30 = 30;
	public static final int M60 = 60;
	public static final int INFO = 42;

	public static Integer EXPLAINIMAGES[] = {
			R.drawable.wiz1,
			R.drawable.wiz2,
			R.drawable.wiz3,
			R.drawable.wiz4,
			R.drawable.wiz5,
			R.drawable.wiz6,
			R.drawable.wiz7,
			R.drawable.wiz8

	};
	public static String EXPLAINHEADERS[] = {
			"Create connection",
			"Use Sidenavigation",
			"Access Log",
			"Select Namespace",
			"Swipe to Delete",
			"View Resources",
			"Terms and Conditions",
			"Ready to Go"
	};
	public static String EXPLAININFOS[] = {
			"You can directly connect the Kubernetes ApiServer if possible or via SSH.",
			"First the Pod list is loaded but sidenavigation leads you to all resources.",
			"On Pod list click to follow the Pod log, Pod description is also avaliable there via menu.",
			"Select a concrete from Namespaces list if you want to filter results.",
			"Pods, Deployments and Services can be deleted directly via doubleswipe, all other are readonly.",
			"All listed Resources can be viewed by click or menu.",
			"Please accept the terms and conditions.",
			"Enjoy."
	};

	public static String DF_1 ="yyyy.MM.dd G 'at' HH:mm:ss z"; //---- 2001.07.04 AD at 12:08:56 PDT
	public static String DF_2 ="hh 'o''clock' a, zzzz"; // ----------- 12 o'clock PM, Pacific Daylight Time
	public static String DF_3 ="EEE, d MMM yyyy HH:mm:ss Z"; //------- Wed, 4 Jul 2001 12:08:56 -0700
	public static String DF_4 ="yyyy-MM-dd'T'HH:mm:ss.SSSZ"; //------- 2001-07-04T12:08:56.235-0700
	public static String DF_5 ="yyMMddHHmmssZ"; //-------------------- 010704120856-0700
	public static String DF_6 ="K:mm a, z"; // ----------------------- 0:08 PM, PDT
	public static String DF_7 ="h:mm a"; // -------------------------- 12:08 PM
	public static String DF_8 ="EEE, MMM d, ''yy"; // ---------------- Wed, Jul 4, '01
	public static String DF_9 ="HH:mm"; // -------------------------- 12:08
	public static String DF_10 ="MMM d, HH:mm"; // ---------------- Jul 4, 12:08
	public static String DF_11 ="HH:mm:ss"; // -------------------------- 12:08

	public static final String SSHSERVER = "SSH";
	public static final String APISERVER = "API";
    public static final String KUBECONFIG = "Kubeconfig";

	public static final String RES_POD = "Pod";
	public static final String RES_DEPLOYMENT = "Deployment";
	public static final String RES_SERVICE = "Service";
	public static final String RES_NAMESPACE = "Namespace";
	public static final String RES_RESOURCES = "Other Resources";
	public static final String RES_REPLICASET = "Replicaset";
	public static final String RES_NODE = "Node";

	public static final String CMD_DELETE = "delete";
	public static final String CMD_LIST = "list";
	public static final String CMD_GET = "get";

	public static final String THEME_STANDARD = "standard";
	public static final String THEME_COMPUTE_NOIR = "computenoir";
	public static final String THEME_GRADIENT = "gradient";

}
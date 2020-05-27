/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.model;

import java.util.ArrayList;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import me.snowdrop.istio.client.DefaultIstioClient;
/**
 * <B>Class: ResourceHelper </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: A mapping and central handling of the k8s/istio resources <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */

public class ResourceHelper {

    private static final String TAG = ResourceHelper.class.getSimpleName();
    //K8s Resources
    public final static String componentstatuses = "ComponentStatuses";
    public final static String configmaps = "ConfigMaps";
    public final static String endpoints = "Endpoints";
    public final static String events = "Events";
    public final static String limitranges = "LimitRanges";
    public final static String namespaces = "Namespaces";
    public final static String nodes = "Nodes";
    public final static String persistentvolumeclaims = "PersistentVolumeClaims";
    public final static String persistentvolumes = "PersistentVolumes";
    public final static String pods = "Pods";
    public final static String podtemplates = "PodTemplates";
    public final static String replicationcontrollers = "ReplicationControllers";
    public final static String resourcequotas = "ResourceQuotas";
    public final static String secrets = "Secrets";
    public final static String serviceaccounts = "ServiceAccounts";
    public final static String services = "Services";
    public final static String mutatingwebhookconfigurations = "MutatingWebhookConfigurations";
    public final static String validatingwebhookconfigurations = "ValidatingWebhookConfigurations";
    public final static String customresourcedefinitions = "CustomResourceDefinitions";
    public final static String apiservices = "APIServices";
    public final static String controllerrevisions = "ControllerRevisions";
    public final static String daemonsets = "DaemonSets";
    public final static String deployments = "Deployments";
    public final static String replicasets = "ReplicaSets";
    public final static String statefulsets = "StatefulSets";
    public final static String meshpolicies = "MeshPolicies";
    public final static String policies = "Policies";
    public final static String tokenreviews = "TokenReviews";
    public final static String localsubjectaccessreviews = "LocalSubjectAccessReviews";
    public final static String selfsubjectaccessreviews = "SelfSubjectAccessReviews";
    public final static String selfsubjectrulesreviews = "SelfSubjectRulesReviews";
    public final static String subjectaccessreviews = "SubjectAccessReviews";
    public final static String horizontalpodautoscalers = "HorizontalPodAutoscalers";
    public final static String cronjobs = "CronJobs";
    public final static String jobs = "Jobs";
    public final static String certificatesigningrequests = "CertificateSigningRequests";
    public final static String certificates = "Certificates";
    public final static String challenges = "Challenges";
    public final static String clusterissuers = "ClusterIssuers";
    public final static String issuers = "Issuers";
    public final static String orders = "Orders";
    public final static String leases = "Leases";
    public final static String ingresses = "Ingresses";
    public final static String networkpolicies = "NetworkPolicies";
    public final static String runtimeclasses = "RuntimeClasses";
    public final static String poddisruptionbudgets = "PodDisruptionBudgets";
    public final static String podsecuritypolicies = "PodSecurityPolicies";
    public final static String clusterrolebindings = "ClusterRoleBindings";
    public final static String clusterroles = "ClusterRoles";
    public final static String rolebindings = "RoleBindings";
    public final static String roles = "Roles";
    public final static String priorityclasses = "PriorityClasses";
    public final static String csidrivers = "CSIDriveres";
    public final static String csinodes = "CSINodes";
    public final static String storageclasses = "StorageClasses";
    public final static String volumeattachments = "VolumeAttachments";

    public final static String[] KUBERNETES_RESOURES = new String[]{componentstatuses, configmaps, endpoints, events, namespaces, nodes,
            secrets, services, daemonsets, replicasets, statefulsets,
            horizontalpodautoscalers, cronjobs, jobs,
            ingresses, networkpolicies, clusterroles, clusterrolebindings, roles};

    // ISTIO Resources
    public final static String mixers = "Mixers";
    public final static String attributemanifests = "Attributemanifests";
    public final static String handlers = "Handlers";
    public final static String httpapispecbindings = "HTTPAPISpecBindings";
    public final static String httpapispecs = "HTTPAPISpecs";
    public final static String instances = "Instances";
    public final static String quotaspecbindings = "QuotaSpecBindingss";
    public final static String quotaspecs = "QuotaSpecs";
    public final static String rules = "Rules";
    public final static String templates = "Templates";
    public final static String destinationrules = "DestinationRules";
    public final static String envoyfilters = "EnvoyFilters";
    public final static String gateways = "Gateways";
    public final static String serviceentries = "ServiceEntries";
    public final static String sidecars = "Sidecars";
    public final static String virtualservices = "VirtualServices";
    public final static String authorizationpolicies = "AuthorizationPolicies";
    public final static String clusterrbacconfigs = "ClusterRbacConfigs";
    public final static String rbacconfigs = "RbacConfigs";
    public final static String servicerolebindings = "ServiceRoleBindings";
    public final static String serviceroles = "ServiceRoles";

    public final static String[] ISTIO_RESOURES = new String[]{rules, destinationrules, envoyfilters, gateways,
            serviceentries, virtualservices, serviceroles};

    public HasMetadata getConcreteResourceItem(ArrayList<HasMetadata> actualList, String resourceIdent, String concreteResource) {
        HasMetadata object = actualList.stream()
                .filter(item -> item.getMetadata().getName().equals(concreteResource))
                .findAny()
                .orElse(null);
        return object;
    }

    public ArrayList<HasMetadata> getConcreteResourceItems(KubernetesClient client, DefaultIstioClient defaultIstioClient, String resourceIdent) {
        ArrayList<HasMetadata> result = new ArrayList<>();
        switch (resourceIdent) {
            case componentstatuses:
                result = new ArrayList<HasMetadata>(client.componentstatuses().list().getItems());
                break;
            case configmaps:
                result = new ArrayList<HasMetadata>(client.configMaps().list().getItems());
                break;
            case endpoints:
                result = new ArrayList<HasMetadata>(client.endpoints().list().getItems());
                break;
            case events:
                result = new ArrayList<HasMetadata>(client.events().list().getItems());
                break;
            case namespaces:
                result = new ArrayList<HasMetadata>(client.namespaces().list().getItems());
                break;
            case nodes:
                result = new ArrayList<HasMetadata>(client.nodes().list().getItems());
                break;
            case secrets:
                result = new ArrayList<HasMetadata>(client.secrets().list().getItems());
                break;
            case services:
                result = new ArrayList<HasMetadata>(client.services().list().getItems());
                break;
            case daemonsets:
                result = new ArrayList<HasMetadata>(client.apps().daemonSets().list().getItems());
                break;
            case replicasets:
                result = new ArrayList<HasMetadata>(client.apps().replicaSets().list().getItems());
                break;
            case statefulsets:
                result = new ArrayList<HasMetadata>(client.apps().statefulSets().list().getItems());
                break;
            case horizontalpodautoscalers:
                result = new ArrayList<HasMetadata>(client.autoscaling().horizontalPodAutoscalers().list().getItems());
                break;
            case cronjobs:
                result = new ArrayList<HasMetadata>(client.batch().cronjobs().list().getItems());
                break;
            case jobs:
                result = new ArrayList<HasMetadata>(client.batch().jobs().list().getItems());
                break;
            case ingresses:
                result = new ArrayList<HasMetadata>(client.extensions().ingresses().list().getItems());
                break;
            case networkpolicies:
                result = new ArrayList<HasMetadata>(client.network().networkPolicies().list().getItems());
                break;
            case clusterroles:
                result = new ArrayList<HasMetadata>(client.rbac().clusterRoles().list().getItems());
                break;
            case clusterrolebindings:
                result = new ArrayList<HasMetadata>(client.rbac().clusterRoleBindings().list().getItems());
                break;
            case roles:
                result = new ArrayList<HasMetadata>(client.rbac().roles().list().getItems());
                break;
            case mixers:
                result = new ArrayList<HasMetadata>(defaultIstioClient.mixer().listEntry().list().getItems());
                break;
            case rules:
                result = new ArrayList<HasMetadata>(defaultIstioClient.rule().list().getItems());
                break;
            case destinationrules:
                result = new ArrayList<HasMetadata>(defaultIstioClient.destinationRule().list().getItems());
                break;
            case envoyfilters:
                result = new ArrayList<HasMetadata>(defaultIstioClient.envoyFilter().list().getItems());
                break;
            case gateways:
                result = new ArrayList<HasMetadata>(defaultIstioClient.gateway().list().getItems());
                break;
            case serviceentries:
                result = new ArrayList<HasMetadata>(defaultIstioClient.serviceEntry().list().getItems());
                break;
            case virtualservices:
                result = new ArrayList<HasMetadata>(defaultIstioClient.virtualService().list().getItems());
                break;
            case serviceroles:
                result = new ArrayList<HasMetadata>(defaultIstioClient.serviceRole().list().getItems());
                break;
            default:
                result = null;
        }

        return result;
    }

    public ArrayList<HasMetadata> getResourcesList(boolean istioInstalled) {
        ArrayList<HasMetadata> result = new ArrayList<>();
        for (String s : KUBERNETES_RESOURES) {
            Pod p = new Pod();
            ObjectMeta meta = new ObjectMeta();
            p.setMetadata(meta);
            p.getMetadata().setName(s);
            p.getMetadata().setAdditionalProperty("k8s", true);
            result.add(p);
        }
        if (istioInstalled) {
            for (String s : ISTIO_RESOURES) {
                Pod p = new Pod();
                ObjectMeta meta = new ObjectMeta();
                p.setMetadata(meta);
                p.getMetadata().setName(s);
                p.getMetadata().setAdditionalProperty("istio", true);
                result.add(p);
            }
        }
        return result;
    }

}

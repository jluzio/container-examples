# Ingress

## References
https://kubernetes.io/docs/concepts/services-networking/ingress/
https://kubernetes.io/docs/concepts/services-networking/ingress-controllers/

https://doc.traefik.io/traefik/v2.0/providers/kubernetes-ingress/
https://doc.traefik.io/traefik/v2.0/user-guides/crd-acme/
https://kubernetes.io/docs/tasks/access-application-cluster/ingress-minikube/

## Test Locally
Add a rule to the hosts using the IP Address given by Ingress (for single nodes, it will be always that node IP)

~~~text
# Kubernetes testing
123.123.123.123 myapp.com
# Kubernetes testing (end)
~~~

## Testing with Kubernetes Dashboard
*Warning*: may require HTTPS.

### Install Dashboard
https://github.com/kubernetes/dashboard

- http
~~~bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.6.0/aio/deploy/alternative.yaml
~~~

- https
~~~bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.6.0/aio/deploy/recommended.yaml
~~~

### Create user to access
https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/creating-sample-user.md

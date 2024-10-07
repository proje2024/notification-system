# Bildirim Sistemi

Bu proje, REST API, veritabanı yapılandırması, e-posta gönderme, Keycloak ile kimlik doğrulaması ve bir frontend istemcisi ile birlikte çalışan, Minikube entegrasyonunu içeren bir bildirim sistemidir. Bildirimler socket bağlantısı üzerinden iletilmektedir. Aşağıda, uygulamanın yapılandırılması ve çalıştırılması için adım adım bir kılavuz bulabilirsiniz.

## Gereksinimler

- Docker
- Minikube
- kubectl

## Yapılandırma

Uygulama yapılandırması Kubernetes üzerinde yönetilmektedir. Her servis için gerekli ortam değişkenleri, proje ana dizinindeki `kubernetes` klasörü altında yer alan ilgili servis dizinlerinde tanımlanmış `.env` dosyalarından yüklenmektedir.

### Kubernetes Yapılandırması

Her bir servis için Kubernetes yapılandırma dosyaları (`deployment.yaml` ve `service.yaml`) ilgili servis dizininde bulunmaktadır. Bu dosyalar, Kubernetes cluster'ında gerekli kaynakların oluşturulmasını sağlar. Örneğin:

- `keycloak` servisi için ayarlar `kubernetes/keycloak/` dizininde,
- `api` servisi için ayarlar `kubernetes/api/` dizininde,
- `frontend` servisi için ayarlar `kubernetes/frontend/` dizininde,
- `pgadmin` servisi için ayarlar `kubernetes/pgadmin/` dizininde,
- `postgres` servisi için ayarlar `kubernetes/postgres/` dizininde yer alır.

Her servisin .env dosyası, ilgili servis için hem Kubernetes yapılandırmalarında kullanılacak ortam değişkenlerini hem de servisin kendi çalışma konfigürasyonunu belirten ortam değişkenlerini tanımlar. Bu dosyalar, ilgili servis dizinlerinde yer alır ve bu servislerin Kubernetes üzerindeki çalıştırma parametrelerini ve yapılandırmalarını sağlar.

## Servislerin Ortam Değişkenleri ve Yapılandırmaları

Aşağıda her bir servisin `.env` dosyasındaki değişkenler ve bu değişkenlerin ne işe yaradıkları açıklanmıştır.

### Keycloak Servisi (`kubernetes/keycloak/.env`)

- **DEPLOYMENT_NAME**: Kubernetes deployment'ı için kullanılan isim.
- **APP_NAME**: Uygulama adı, bu isim Kubernetes kaynaklarını etiketlemek için kullanılır.
- **CONTAINER_NAME**: Docker konteynerinin adı.
- **IMAGE_NAME**: Kullanılan Docker imajının adı.
- **IMAGE_VERSION**: Docker imajının sürüm etiketi.
- **TZ**: Uygulamanın çalışacağı zaman dilimi.
- **KEYCLOAK_ADMIN**: Keycloak yönetici kullanıcı adı.
- **KEYCLOAK_ADMIN_PASSWORD**: Keycloak yönetici şifresi.
- **KEYCLOAK_HOSTNAME**: Keycloak servisinin dışarıya açılacağı hostname.
- **KEYCLOAK_PORT**: Keycloak servisinin dış dünyaya açılacağı port.
- **KEYCLOAK_DB_URL**: Keycloak servisi için PostgreSQL veritabanı bağlantı URL'si.
- **KEYCLOAK_DB_USERNAME**: Keycloak veritabanı kullanıcı adı.
- **KEYCLOAK_DB_PASSWORD**: Keycloak veritabanı şifresi.
- **DOCKER_INTERNAL_PORT**: Keycloak servisi için konteyner içinde dinlenen port.
- **KEYCLOAK_NODE_PORT**: Kubernetes node üzerinde Keycloak servisinin dış dünyaya açılan portu.
- **PVC_NAME**: Keycloak için Persistent Volume Claim adı.
- **STORAGE_SIZE**: PVC için ayrılan depolama alanı.

Keycloak servisi yapılandırırken ilgili client ayarında bulunan`Capability config` bölümünde yer alan "Client authentication" değerinin "On" ve "Standard flow", "Direct access grants", "Service accounts roles" alanlarının seçili olduğundan emin olunuz. Yine ilgili client'in `Service account roles` kısmından bulunan"Assign role" yazısına tıklayarak açılan ekranın filtreleme kısmında yer alan "Filter by clients" filtresini seçin. Sonrasında açılan ekranda yer alan "view-users" rolüne tıklayarak rol ataması yapınız.

### API Servisi (`kubernetes/api/.env`)

- **DEPLOYMENT_NAME**: Kubernetes deployment'ı için kullanılan isim.
- **APP_NAME**: Uygulama adı, bu isim Kubernetes kaynaklarını etiketlemek için kullanılır.
- **CONTAINER_NAME**: Docker konteynerinin adı.
- **IMAGE_NAME**: Kullanılan Docker imajının adı.
- **IMAGE_VERSION**: Docker imajının sürüm etiketi.
- **DOCKER_INTERNAL_PORT**: API servisinin konteyner içinde dinlediği port.
- **API_PORT**: Kubernetes servisinin dışa açacağı port.
- **API_NODE_PORT**: Kubernetes node'u üzerinde erişilebilecek port.
- **SERVICE_NAME**: API için Kubernetes servis adı.
- **SPRING_DATASOURCE_URL**: PostgreSQL veritabanı için JDBC bağlantı URL'si.
- **SPRING_DATASOURCE_USERNAME**: Veritabanı kullanıcı adı.
- **SPRING_DATASOURCE_PASSWORD**: Veritabanı şifresi.
- **TZ**: Uygulamanın çalışacağı zaman dilimi.
- **SPRING_JPA_HIBERNATE_DDL_AUTO**: Hibernate'in veritabanı üzerindeki işlem türü.
- **SPRING_JPA_SHOW_SQL**: SQL sorgularının loglanmasını sağlar.
- **HIBERNATE_DIALECT**: Hibernate'in kullanacağı diyalekt.
- **API_HOST**: API servisi için kullanılacak hostname.
- **FRONTEND_HOST**: Frontend servisi için kullanılacak hostname.
- **FRONTEND_PORT**: Frontend servisinin dinlediği port.
- **FILE_UPLOAD_DIR**: Yüklenen dosyaların saklanacağı dizin.
- **FILE_SIZE**: İzin verilen maksimum dosya boyutu.
- **SPRING_SERVLET_MULTIPART_ENABLED**: Çoklu parça yüklemelerini etkinleştirir.
- **SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE**: Maksimum dosya boyutu.
- **SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE**: Maksimum istek boyutu.
- **LOGGING_LEVEL_ROOT**: Root seviyesinde loglama seviyesi.
- **LOGGING_LEVEL_APP**: Uygulama için loglama seviyesi.
- **SPRING_JPA_OPEN_IN_VIEW**: Open-in-view özelliğini devre dışı bırakır.
- **SPRING_MAIL_HOST**: SMTP sunucu hostu.
- **SPRING_MAIL_PORT**: SMTP sunucu portu.
- **SPRING_MAIL_USERNAME**: SMTP kullanıcı adı.
- **SPRING_MAIL_PASSWORD**: SMTP kullanıcı şifresi.
- **SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH**: SMTP kimlik doğrulamasını etkinleştirir.
- **SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE**: TLS bağlantısını etkinleştirir.
- **POSTGRES_USER**: PostgreSQL kullanıcı adı.
- **POSTGRES_PASSWORD**: PostgreSQL şifresi.
- **POSTGRES_DB**: Kullanılacak veritabanı adı.
- **KEYCLOAK_HOST**: Keycloak sunucusu için kullanılacak host.
- **KEYCLOAK_PORT**: Keycloak sunucusunun dinlediği port.
- **KEYCLOAK_REALM**: Keycloak realm adı.
- **KEYCLOAK_CLIENT**: Keycloak client adı.
- **KEYCLOAK_SECRET**: Keycloak client secret.
- **REDIRECT_URI**: Başarılı kimlik doğrulama sonrası yönlendirilecek URI.
- **FRONTEND_SERVICE_NAME**: Frontend servisi adı.
- **FRONTEND_SERVICE_URL**: Frontend servisi için URL.
- **FRONTEND_LOCAL_URL**: Frontend uygulaması için yerel URL.

### Frontend Servisi (`kubernetes/frontend/.env`)

- **DEPLOYMENT_NAME**: Kubernetes deployment'ı için kullanılan isim.
- **APP_NAME**: Uygulama adı, bu isim Kubernetes kaynaklarını etiketlemek için kullanılır.
- **CONTAINER_NAME**: Docker konteynerinin adı.
- **IMAGE_NAME**: Kullanılan Docker imajının adı.
- **IMAGE_VERSION**: Docker imajının sürüm etiketi.
- **DOCKER_INTERNAL_PORT**: Frontend servisi için konteyner içinde dinlenen port.
- **EXTERNAL_PORT**: Frontend servisi için dış dünyaya açılan port.
- **FRONTEND_NODE_PORT**: Kubernetes node'u üzerinde erişilebilecek port.
- **SERVICE_NAME**: Frontend için Kubernetes servis adı.
- **NGINX_HOST**: Nginx'in proxy yapacağı API servisi hostu.
- **NGINX_PORT**: Nginx'in proxy yapacağı API servisi portu.
- **NGINX_LISTEN**: Nginx'in dinleyeceği port.
- **NGINX_SERVER_NAME**: Nginx server adı.
- **NGINX_FILE_SIZE**: İzin verilen maksimum dosya boyutu.

### React Frontend Çevre Değişkenleri (`frontend/.env`)

- **REACT_APP_KEYCLOAK_HOST**: Keycloak sunucusu için kullanılacak host.
- **REACT_APP_KEYCLOAK_PORT**: Keycloak sunucusunun dinlediği port.
- **REACT_APP_KEYCLOAK_REALM**: Keycloak realm adı.
- **REACT_APP_KEYCLOAK_CLIENT**: Keycloak client adı.
- **REACT_APP_REDIRECT_URI**: Başarılı kimlik doğrulama sonrası yönlendirilecek URI.

### PgAdmin Servisi (`kubernetes/pgadmin/.env`)

- **DEPLOYMENT_NAME**: Kubernetes deployment'ı için kullanılan isim.
- **APP_NAME**: Uygulama adı, bu isim Kubernetes kaynaklarını etiketlemek için kullanılır.
- **CONTAINER_NAME**: Docker konteynerinin adı.
- **IMAGE_NAME**: Kullanılan Docker imajının adı.
- **IMAGE_VERSION**: Docker imajının sürüm etiketi.
- **DOCKER_INTERNAL_PORT**: PgAdmin servisi için konteyner içinde dinlenen port.
- **PGADMIN_PORT**: PgAdmin servisi için dış dünyaya açılan port.
- **PGADMIN_DEFAULT_EMAIL**: PgAdmin'e giriş yapmak için kullanılan varsayılan e-posta adresi.
- **PGADMIN_DEFAULT_PASSWORD**: PgAdmin'e giriş yapmak için kullanılan varsayılan şifre.
- **TZ**: Uygulamanın çalışacağı zaman dilimi.
- **SERVICE_NAME**: PgAdmin servisi için kullanılacak Kubernetes servis adı.

### PostgreSQL Servisi (`kubernetes/postgres/.env`)

- **DEPLOYMENT_NAME**: Kubernetes deployment'ı için kullanılan isim.
- **APP_NAME**: Uygulama adı, bu isim Kubernetes kaynaklarını etiketlemek için kullanılır.
- **CONTAINER_NAME**: Docker konteynerinin adı.
- **IMAGE_NAME**: Kullanılan Docker imajının adı.
- **IMAGE_VERSION**: Docker imajının sürüm etiketi.
- **DOCKER_INTERNAL_PORT**: PostgreSQL servisi için konteyner içinde dinlenen port.
- **DB_PORT**: PostgreSQL servisi için dış dünyaya açılan port.
- **POSTGRES_USER**: PostgreSQL kullanıcı adı.
- **POSTGRES_PASSWORD**: PostgreSQL şifresi.
- **POSTGRES_DB**: Kullanılacak veritabanı adı.
- **TZ**: Uygulamanın çalışacağı zaman dilimi.
- **PVC_NAME**: PersistentVolumeClaim (PVC) için kullanılan isim.
- **STORAGE_SIZE**: PVC için ayrılan depolama alanı.
- **SERVICE_NAME**: PostgreSQL servisi için kullanılacak Kubernetes servis adı.

### PostgreSQL ve Keycloak için Persistent Volume Claim (PVC)

PostgreSQL ve Keycloak servisleri için veritabanı verileri kalıcı olarak saklanır. Bu servisler, Persistent Volume Claim (PVC) kullanarak kalıcı depolama alanı talep ederler. PVC, veritabanı verilerinin silinme veya servis yeniden başlatılması gibi durumlarda korunmasını sağlar. Her iki servis de belirlenen depolama alanını kullanarak verilerini güvenli bir şekilde saklar.

## Kurulum ve Çalıştırma Adımları

Bu bölüm, projenin gereksinimlerinin nasıl indirileceğini, kurulacağını ve her bir servisin nasıl çalıştırılacağını adım adım açıklar. Bu kılavuz, Kubernetes üzerinde projenizi çalıştırmak için gerekli tüm bilgileri sağlar.

### Gereksinimlerin Kurulumu

1.  **Docker Kurulumu:**

    Docker'ı sisteminize kurmak için [Docker'ın resmi sitesindeki](https://docs.docker.com/get-docker/) talimatları izleyin. Kurulum tamamlandıktan sonra, Docker'ın çalıştığını doğrulamak için terminalden şu komutu çalıştırabilirsiniz:

    ```bash
    docker --version
    ```

2.  **Minikube Kurulumu:**

    Minikube, Kubernetes cluster'ını yerel olarak çalıştırmanızı sağlar. Minikube'u kurmak için aşağıdaki adımları izleyin:

    - Linux için Minikube'u indirin ve yükleyin:

      ```bash
      curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 \
      && sudo install minikube-linux-amd64 /usr/local/bin/minikube
      ```

    - Minikube'u başlatın:

      ```bash
      minikube start --driver=kvm2
      ```

      ### Eğer `minikube start --driver=kvm2` ile başlatamazsanız:

      1. **KVM ve libvirt kurulumunu doğrulayın:**

      ```bash
      sudo apt update
      sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils
      sudo systemctl enable libvirtd
      sudo systemctl start libvirtd
      ```

      2. **Kullandığınız kullanıcıyı `libvirt` grubuna ekleyin:**

      ```bash
      sudo usermod -aG libvirt $(whoami)
      newgrp libvirt
      ```

      3. **KVM'nin etkin olup olmadığını kontrol edin:**

      ```bash
      kvm-ok
      ```

      4. **Daha sonra tekrar çalıştırmayı deneyin**

      ```bash
      minikube start --driver=kvm2
      ```

    - Docker'ı Minikube ile kullanmak için aşağıdaki komutu çalıştırın:

      ```bash
      eval $(minikube -p minikube docker-env)
      ```

3.  **kubectl Kurulumu:**

    Kubernetes cluster'ınızı yönetmek için kubectl aracını kullanmanız gerekmektedir. Kubectl'i kurmak için aşağıdaki adımları izleyin:

    - Linux için kubectl'i indirin ve yükleyin:

      ```bash
      curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
      sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
      ```

    - Kurulumu doğrulamak için:

      ```bash
      kubectl version --client
      ```

### Projeyi Çalıştırma

Projeyi Kubernetes üzerinde çalıştırmak için aşağıdaki adımları takip edin:

1. **Docker İmajlarını Oluşturun:**

   Proje dizininde, Dockerfile'ları kullanarak gerekli Docker imajlarını oluşturun. Örneğin:

   ```bash
   docker build -t notification-api:v.28.08.24.1 -f Dockerfile.api .
   docker build -t notification-frontend:v.28.08.24.1 -f Dockerfile.client .
   ```

   Oluşturduğunuz docker image'leri minikube içindeki docker ortamına yükleyin. Örneğin:

   ```bash
   docker save notification-api:v.28.08.24.1 | (eval $(minikube docker-env) && docker load)
   docker save notification-frontend:v.28.08.24.1 | (eval $(minikube docker-env) && docker load)
   ```

   Burada yazdığınız image ismi ve tag'lerini ilgili servisin .env dosyasında değiştirmeyi unutmayınız. Örneğin:

   ```bash
   docker build -t notification-api:v.28.08.24.1 -f kubernetes/api/Dockerfile .
   ```

   yukarıdaki komutta yer alan image ismi ve tag'i doğrultusunda kubernetes/api/.env dosyası içerisinde yer alan:

   ```bash
   IMAGE_NAME=notification-api
   IMAGE_VERSION=v.28.08.24.1
   ```

   ortam değişkenlerini değiştiriniz.

2. **Kubernetes Yapılandırmalarını Uygulayın:**

   Her bir servis için ilgili `.env` dosyasını yükleyerek Kubernetes deployment ve service'lerini oluşturun.

   Lütfren aşağıdaki sırada oluşturunuz.

   - **PostgreSQL Servisini Çalıştırmak için:**

   Öncelikle Persistent Volume Claim (PVC) oluşturun:

   ```bash
   export $(cat kubernetes/postgres/.env | xargs)  # Ortam değişkenlerini yükleyin
   envsubst < kubernetes/postgres/postgres-pvc.yaml | kubectl apply -f -
   ```

   Daha sonra PostgreSQL servisini çalıştırın:

   ```bash
   envsubst < kubernetes/postgres/postgres-deployment.yaml | kubectl apply -f -
   envsubst < kubernetes/postgres/postgres-service.yaml | kubectl apply -f -
   envsubst < kubernetes/postgres/postgres-statefulset.yaml | kubectl apply -f -
   ```

   - **Keycloak Servisini Çalıştırmak için:**

     ```bash
     export $(cat kubernetes/keycloak/.env | xargs)  # Ortam değişkenlerini yükleyin
     envsubst < kubernetes/keycloak/keycloak-pvc.yaml | kubectl apply -f -
     envsubst < kubernetes/keycloak/keycloak-deployment.yaml | kubectl apply -f -
     envsubst < kubernetes/keycloak/keycloak-service.yaml | kubectl apply -f -
     ```

   - **Infinispan Servisini Çalıştırmak için:**

     ```bash
      export $(cat kubernetes/infinispan/.env | xargs)  # Ortam değişkenlerini yükleyin
      envsubst < kubernetes/infinispan/infinispan-deployment.yaml | kubectl apply -f -
      envsubst < kubernetes/infinispan/infinispan-service.yaml | kubectl apply -f -

     ```

   - **API Servisini Çalıştırmak için:**

     ```bash
     export $(cat kubernetes/api/.env | xargs)  # Ortam değişkenlerini yükleyin
     envsubst < kubernetes/api/api-deployment.yaml | kubectl apply -f -
     envsubst < kubernetes/api/api-service.yaml | kubectl apply -f -
     ```

   - **Frontend Servisini Çalıştırmak için:**

     ```bash
     export $(cat kubernetes/frontend/.env | xargs)  # Ortam değişkenlerini yükleyin
     envsubst < kubernetes/frontend/frontend-deployment.yaml | kubectl apply -f -
     envsubst < kubernetes/frontend/frontend-service.yaml | kubectl apply -f -
     ```

   - **PgAdmin Servisini Çalıştırmak için:**

     ```bash
     export $(cat kubernetes/pgadmin/.env | xargs)  # Ortam değişkenlerini yükleyin
     envsubst < kubernetes/pgadmin/pgadmin-deployment.yaml | kubectl apply -f -
     envsubst < kubernetes/pgadmin/pgadmin-service.yaml | kubectl apply -f -
     ```

3. **Servislerin Durumunu Kontrol Edin:**

   Kubernetes üzerinde servislerin durumunu kontrol etmek için:

   ```bash
   kubectl get pods
   kubectl get services
   ```

   Bu komutlar, tüm pod'ların ve servislerin doğru bir şekilde çalışıp çalışmadığını gösterecektir.

4. **Servislere Erişim:**

   Minikube ile yerel olarak çalışan servislere erişmek için `minikube service` komutunu kullanabilirsiniz. Örneğin, API servisine erişmek için:

   ```bash
   minikube service api-service
   ```

5. **Logları İnceleyin:**

   Herhangi bir sorunla karşılaşırsanız, pod loglarını incelemek için:

   ```bash
   kubectl logs <pod-name>
   ```

   İstediğiniz portu frontende yönlendirmek için:

   ```bash
   kubectl port-forward service/frontend-service 3000:80
   ```

Bu adımları takip ederek, projeyi Kubernetes üzerinde başarıyla çalıştırabilirsiniz.

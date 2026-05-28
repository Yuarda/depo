# 📚 Kelime Ezberleme Uygulaması (Word Learning App)

Bu proje, kullanıcıların Aralıklı Tekrar (Spaced Repetition) algoritmasını kullanarak yabancı dil kelimelerini kalıcı olarak öğrenmelerini sağlayan uçtan uca (end-to-end) bir mobil uygulamadır.

## 🌟 Temel Özellikler
* **Akıllı Tekrar Algoritması:** Sistem günlük olarak yeni kelimeleri ve üstüne ek olarak tarihi gelmiş (öğrenilme sürecindeki) kelimeleri tekrar kullanıcıya mecburi olarak sorar, böylece kalıcı öğrenme sağlanır. Yanlış bilinen kelimelerin tekrar süreci sıfırdan başlar.
* **Modern Mimari:** Python (FastAPI) tabanlı güçlü ve hızlı bir Backend.
* **Kullanıcı Dostu Arayüz:** Kotlin ile geliştirilmiş Native Android deneyimi.
* **Gelişmiş İstatistikler:** Kullanıcı gelişimini takip eden görsel veri analizleri.

## 🛠️ Kullanılan Teknolojiler (Tech Stack)
* **Backend:** Python, FastAPI, SQLAlchemy, Uvicorn, Pydantic
* **Frontend:** Kotlin, Android Studio, XML
* **Veritabanı:** SQL Server (pyodbc)
* **Veri Görselleştirme:** Matplotlib
* **Kod Kalitesi:** SonarCloud / SonarQube

## 📂 Proje Yapısı
* `/backend`: FastAPI ile yazılmış Python sunucu kodları ve API uç noktaları (endpoints).
* `/app`: Kotlin ile yazılmış Android arayüz ve istemci (client) kodları.
* `/gradle`: Android projesi için derleme ve bağımlılık yöneticisi dosyaları.
* `veritabani_kurulum.sql`: Yerel SQL Server üzerinde veritabanı tablolarını ve ilişkilerini otomatik oluşturan kurulum betiği.
---

## 🚀 Kurulum ve Çalıştırma Rehberi

Projeyi kendi bilgisayarınızda çalıştırmak için aşağıdaki adımları sırasıyla izleyin:

### 1. Veritabanı Kurulumu
1. Proje ana dizinindeki `veritabani_kurulum.sql` dosyasını SQL Server Management Studio (SSMS) veya benzeri bir araç ile açın.
2. Scripti çalıştırarak gerekli tabloların ve ilişkilerin kendi yerel sunucunuzda oluşmasını sağlayın.

### 2. Backend (API) Kurulumu
Backend tarafının çalışabilmesi için sisteminizde Python yüklü olmalıdır.
1. Terminali açın ve `/backend` klasörüne gidin: `cd backend`
2. Gerekli Python kütüphanelerini kurun:
   ```bash
   pip install fastapi uvicorn sqlalchemy pyodbc matplotlib pydantic python-dateutil
3. main.py dosyasını açıp DATABASE_URL kısmındaki sunucu adını kendi yerel SQL Server adınızla güncelleyin.
4.Sunucuyu ayağa kaldırın: uvicorn main:app --reload

### 3. Frontend (Android/Kotlin) Kurulumu
1. Projeyi Android Studio ile açın.
2. Kritik Not: Mobil uygulamadan API'ye istek atarken URL olarak localhost kullanmayın.
--Android Emulator için: http://10.0.2.2:8000
--Gerçek cihaz için: Aynı Wi-Fi ağına bağlı bilgisayarınızın yerel IP adresi (örn: http://192.168.1.X:8000)


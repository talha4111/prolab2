<div align="center">
  <h1>📈 Sales Predictor — Java OOP & From-Scratch Machine Learning Platform</h1>
  <p><strong>Harici ML Kütüphanesi Kullanmadan Saf Java (Java 11+ & Swing) ile Geliştirilmiş KNN, CART Karar Ağacı (Gini Impurity) ve Performans Analiz Platformu</strong></p>
  
  [![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
  [![GUI](https://img.shields.io/badge/GUI-Java_Swing_%26_Graphics2D-007396?style=for-the-badge)]()
  [![Apache POI](https://img.shields.io/badge/Data-Apache_POI_Excel_%26_CSV-D22128?style=for-the-badge&logo=apache&logoColor=white)](https://poi.apache.org/)
  [![ML](https://img.shields.io/badge/ML_From_Scratch-KNN_%26_Decision_Tree-2EA44F?style=for-the-badge)]()
</div>

<br>

## 📖 Proje Hakkında
**Sales Predictor (`prolab2`)**, perakende ve e-ticaret müşteri/satış veri setlerini (`.xlsx` ve `.csv`) analiz ederek müşterilerin satın alma eğilimlerini ve ürün kategorilerini tahmin eden, **Nesne Yönelimli Programlama (OOP)** ve **SOLID** prensipleriyle geliştirilmiş bir masaüstü makine öğrenmesi uygulamasıdır.

Projede Weka veya harici bir makine öğrenmesi kütüphanesi kullanılmamış; **K-En Yakın Komşu (KNN)** ve **CART Karar Ağacı (Decision Tree — Gini Impurity)** sınıflandırma algoritmaları, veri ön işleme (Min-Max Normalizasyon & Hash Encoding) ve model değerlendirme (Confusion Matrix, JVM Heap Bellek & Süre Analizi) katmanları **tamamen sıfırdan saf Java ile** gerçeklenmiştir.

---

## ✨ Temel Mühendislik Özellikleri

### 1. 🧠 Sıfırdan Gerçeklenen Makine Öğrenmesi Algoritmaları (`src/prolab/salespredict/core/`)
- **K-Nearest Neighbors (`KNNClassifier.java`):**
  - *Tembel Öğrenme (Lazy Learning)* yaklaşımıyla eğitim verisini bellekte tutar.
  - Yeni bir müşteri kaydı geldiğinde 4 boyutlu öznitelik uzayında (`Age`, `City`, `Gender`, `SpendingScore`) **Öklid Mesafesi (Euclidean Distance)** hesaplar:
    $$d(u_1, u_2) = \sqrt{\sum_{i=1}^{4} (x_{1,i} - x_{2,i})^2}$$
  - En yakın $K$ komşuyu `PriorityQueue<Neighbor>` (Min-Heap) veri yapısıyla $O(N \log N)$ karmaşıklıkta sıralar ve çoğunluk oylaması (Majority Voting) ile hedef ürün kategorisini tahmin eder.
- **CART Karar Ağacı (`DecisionTreeClassifier.java`):**
  - Özyineli (recursive) ikili ağaç (`Node`) mimarisi ile veriyi en iyi ayıran öznitelik (`featureIndex`) ve eşik değerini (`threshold`) bulur (`findBestSplit`).
  - Bölünme kalitesini **Gini Safsızlık İndeksi (Gini Impurity)** ile ölçer:
    $$\text{Gini}(D) = 1 - \sum_{k=1}^{C} p_k^2, \quad \text{Gini}_{\text{split}} = \frac{|D_{\text{left}}|}{|D|}\text{Gini}(D_{\text{left}}) + \frac{|D_{\text{right}}|}{|D|}\text{Gini}(D_{\text{right}})$$
  - Aşırı öğrenmeyi (*Overfitting*) önlemek için kullanıcı tarafından yapılandırılabilir `maxDepth` (Maksimum Derinlik) hiperparametresi ile ağacı budar.

### 2. 📊 Veri Yükleme & Ön İşleme Hattı (`src/prolab/salespredict/data/`)
- **Çoklu Format Desteği (`DataLoader.java`):** **Apache POI (`XSSFWorkbook`)** ile `.xlsx` Excel dosyalarını ve yerleşik akış okuyucularla `.csv` veri setlerini dinamik sütun eşleştirme ile okur.
- **Normalizasyon & Kodlama (`PreProcessor.java`):** Kategorik değişkenleri sayısal uzaya dönüştürür ve tüm öznitelikleri $[0, 1]$ aralığına **Min-Max Normalizasyonu** ile ölçeklendirerek Öklid mesafesindeki ölçek baskınlığını ortadan kaldırır.

### 3. ⏱️ Model Değerlendirme & JVM Kaynak Analizi (`src/prolab/salespredict/evaluation/`)
- **Çok Boyutlu Karşılaştırma (`Evaluator.java`):** Veri setini eğitim/test olarak böler; her iki algoritmanın **Genel Doğruluk Oranı (Accuracy %)**, **Kategori Bazlı Başarı**, **Çalışma Süresi (ms)** ve `Runtime.getRuntime()` üzerinden **JVM Heap Bellek Tüketimi (KB)** metriklerini ölçer.

### 4. 🖥️ Multithreaded Java Swing Arayüzü (`src/prolab/salespredict/gui/`)
- **`SwingWorker` ile Asenkron Eğitim:** Büyük veri setlerinde (`MarketSalesKocaeli.csv` / `.xlsx`) arayüzün donmasını (EDT blokajını) önlemek için tüm eğitim ve test süreçlerini arka plan iş parçacıklarında yürütür.
- **Özel 2D Grafik Çizimi (`GraphPanel`):** `Graphics2D` ve `RenderingHints.VALUE_ANTIALIAS_ON` kullanarak KNN ve Decision Tree modellerinin doğruluk ve hız karşılaştırmalarını özel sütun grafikleriyle görselleştirir.

---

## 🚀 Kurulum ve Çalıştırma

### 1. Repoyu Klonlayın
```bash
git clone https://github.com/talha4111/prolab2.git
cd prolab2
```

### 2. Derleme ve Çalıştırma (Windows / Linux / macOS)
Proje kök dizinindeki `lib/` klasöründe Apache POI bağımlılıkları yer almaktadır:

**Windows (PowerShell / CMD):**
```powershell
javac -encoding UTF-8 -cp "lib/*" -d out (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
java -cp "out;lib/*" prolab.salespredict.Main
```

**Linux / macOS:**
```bash
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" prolab.salespredict.Main
```

---

## 📂 Proje Klasör Yapısı
```text
prolab2/
├── src/prolab/salespredict/
│   ├── core/
│   │   ├── IClassifier.java              # Sınıflandırma algoritmaları için temel arayüz (Interface)
│   │   ├── BaseAlgorithm.java            # Doğruluk hesaplama mantığını barındıran soyut sınıf (Abstract Class)
│   │   ├── KNNClassifier.java            # Öklid mesafesi ve PriorityQueue tabanlı KNN gerçeklemesi
│   │   └── DecisionTreeClassifier.java   # Gini Impurity tabanlı özyineli CART Karar Ağacı
│   ├── data/
│   │   ├── DataLoader.java               # Apache POI (.xlsx) ve CSV veri okuma katmanı
│   │   └── PreProcessor.java             # Min-Max Normalizasyon ve öznitelik dönüştürücü
│   ├── evaluation/
│   │   └── Evaluator.java                # Doğruluk, süre (ms) ve JVM bellek (KB) ölçüm motoru
│   ├── gui/
│   │   └── MainFrame.java                # SwingWorker destekli modern masaüstü arayüzü ve Graphics2D grafik paneli
│   ├── model/
│   │   └── UserRecord.java               # Müşteri ve satış kaydı veri modeli (POJO)
│   └── Main.java                         # Uygulama giriş noktası (Thread-safe EDT başlatıcı)
├── lib/                                  # Apache POI ve XMLBeans JAR kütüphaneleri
└── Docs/                                 # Teknik tasarım raporu ve algoritma akış dokümantasyonu
```

---

## 👨‍💻 Geliştirici
**Talha Gürler** — Kocaeli Üniversitesi Bilgisayar Mühendisliği Bölümü — [GitHub (@talha4111)](https://github.com/talha4111)

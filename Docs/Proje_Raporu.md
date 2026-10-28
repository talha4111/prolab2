# Java Tabanlı Nesneye Yönelik Yaklaşımla K-En Yakın Komşu ve Karar Ağacı Algoritmalarının Karşılaştırmalı Analizi

**Talha Gürler, Emir Bera Soğuk**
*Kocaeli Üniversitesi, Bilgisayar Mühendisliği Bölümü*

---

## 1. GİRİŞ
Makine öğrenmesi modelleri, müşteri davranışlarını analiz etmek ve gelecekteki satın alma eğilimlerini tahmin etmek için perakende sektöründe sıkça kullanılmaktadır. Bu projenin temel amacı, Kocaeli merkezli bir marketin gerçek satış verilerini kullanarak, müşterilerin harcama potansiyellerine, aldıkları markaya ve miktar gibi profillerine göre hangi ürün kategorisine yöneleceklerini tahmin eden bir sistem geliştirmektir. 

Sistem geliştirilirken Weka veya Scikit-learn gibi hazır kütüphaneler kullanılmamış, sınıflandırma algoritmalarından K-En Yakın Komşu (KNN) ve Karar Ağacı (Decision Tree) algoritmaları Java programlama dili ile sıfırdan matematiksel temellerine uygun olarak kodlanmıştır. Geliştirilen bu iki model, tahmin doğruluğu (accuracy) ve hesaplama süresi (ms) açısından birbirleriyle karşılaştırılarak analiz edilmiştir.

## 2. VERİ SETİ VE ÖN İŞLEME
Projede kullanılan "MarketSalesKocaeli" veri seti, Türkiye'deki perakende satış noktalarından elde edilmiş 5000'den fazla gerçek satış işlemini (transaction) içermektedir. Veriler algoritmaya verilmeden önce birtakım ön işleme adımlarından geçirilmiştir:

* **Veri Temizleme:** Kategori sütunu boş veya hatalı formatta olan satırlar kodlama aşamasında `try-catch` bloklarıyla yakalanarak atlanmış ve veri setinden temizlenmiştir.
* **Sayısal Dönüştürme (Label Encoding):** Makine öğrenmesi modelleri kategorik verilerle doğrudan işlem yapamadığı için "Brand" (Marka) gibi değişkenler, `hashCode()` mantığına dayalı özel bir metot ile sayısal ID'lere dönüştürülmüştür.
* **Min-Max Normalizasyonu:** KNN algoritmasında mesafe hesaplanırken büyük değerli sütunların (Örn: 50.000 değerindeki toplam tutar), daha küçük değerli sütunları (Örn: 5 adet miktar) baskılamasını engellemek için tüm bağımsız değişkenler Min-Max normalizasyonu formülü kullanılarak `[0-1]` aralığına sıkıştırılmıştır.
* **Eğitim ve Test Ayrımı:** Veri seti rastgele karıştırılarak (shuffle) %80'i eğitim (training), %20'si test verisi olacak şekilde sistem tarafından dinamik olarak ikiye bölünmüştür.

## 3. YÖNTEM
Projeye konu olan iki algoritmanın temel çalışma prensipleri ve kodlama yaklaşımları aşağıda özetlenmiştir.

### 3.1. K-En Yakın Komşu (KNN) Algoritması
KNN, tembel öğrenen (lazy learner) bir modeldir. Modeli eğitmek yerine sadece geçmiş veriyi hafızasında tutar. Yeni bir verinin hangi kategoriden ürün alacağını tahmin etmek için bu veri ile sistemde kayıtlı olan tüm eğitim verileri arasındaki Öklid mesafesi (Euclidean Distance) hesaplanmıştır. Mesafeleri sıralamak ve en kısa mesafeleri hızlıca bulmak amacıyla Java'daki `PriorityQueue` (Öncelikli Kuyruk) veri yapısı kullanılmıştır. Belirlenen `K` adet en yakın komşunun sahip olduğu kategoriler sayılarak çoğunluk oyuna (majority vote) göre tahmin yapılmıştır.

### 3.2. Karar Ağacı (Decision Tree) Algoritması
Karar Ağacı, veriyi en iyi bölen (Gini Impurity yaklaşımı kullanılarak) özellikleri bularak ağaç yapısını özyinelemeli (recursive) bir şekilde aşağıya doğru inşa eden bir mantığa sahiptir. Geliştirilen algoritmada, aşırı öğrenmeyi (overfitting) engellemek amacıyla arayüzden de belirlenebilen bir "Maksimum Derinlik (maxDepth)" kontrolü eklenmiştir. Model, ağacı bir kez inşa ettikten sonra yeni verilerin tahmini sadece kurallara (düğüm ayrımlarına) bakarak yapıldığı için tahminde çok daha hızlı sonuç üretmektedir.

## 4. NESNEYE YÖNELİK PROGRAMLAMA (NYP) MİMARİSİ
Proje, NYP kurallarına sıkı sıkıya bağlı bir mimariyle tasarlanmıştır. Uygulanan başlıca kurallar şunlardır:
* **Arayüz (Interface) ve Çok Biçimlilik (Polymorphism):** Her iki algoritma da zorunlu olarak `train()` ve `predict()` metotlarını barındıran `IClassifier` arayüzünü implemente etmiştir. Bu sayede arayüz kısmında kullanıcı hangi algoritmayı seçerse seçsin, arka planda tek tip bir `IClassifier` referansı üzerinden işlemler hatasız yürütülmüştür.
* **Kapsülleme (Encapsulation):** Satış verilerini temsil eden `UserRecord` sınıfındaki değişkenler `private` olarak tanımlanmış, verilere erişim sadece yapıcı metot (constructor) ve `getter` metotları ile sınırlandırılmıştır.
* **Kalıtım (Inheritance):** Algoritmaların ihtiyaç duyabileceği ortak metotlar kod tekrarını engellemek amacıyla soyut (abstract) olarak tasarlanan `BaseAlgorithm` sınıfında toplanmıştır.

## 5. DENEYSEL SONUÇLAR
Uygulama üzerinden gerçekleştirilen testlerde aşağıdaki bulgular elde edilmiştir:

**KNN Sonuçları:**
* Eğitim Süresi: ~0-5 ms (Sadece veriyi değişkene atar)
* Tahmin Süresi: Yüksek (Her test verisi için tüm eğitim setiyle tek tek matematiksel işlem yapar)
* Doğruluk (Accuracy): Normalizasyon sonrası ~%80-90 bandında dengeli sonuçlar gözlemlenmiştir.

**Karar Ağacı Sonuçları:**
* Eğitim Süresi: Göreceli Yavaş (Her bir bölünme için Gini safsızlık hesaplamalarından ötürü ağaç inşası vakit alır)
* Tahmin Süresi: Çok hızlı (Milisaniyenin altında if-else geçişleri ile karar verir)
* Doğruluk (Accuracy): `maxDepth` kısıtlamasına bağlı olmak üzere yine KNN ile yarışacak seviyede yüksek (%85-90 civarı) doğruluk tespit edilmiştir. Ancak derinlik çok artırıldığında test verisi üzerindeki başarının düştüğü gözlemlenmiştir.

Geliştirilen arayüz üzerinde yer alan Kategori Bazlı Başarı Tablosu ve Sütun Grafiği (Bar Chart) sayesinde, algoritmaların spesifik kategorilerde (Örn: Gıda, Kozmetik, Deterjan) ne kadar doğru veya yanlış tahmin yaptığı görselleştirilmiştir.

## 6. DEĞERLENDİRME
Sonuçlar incelendiğinde, K-En Yakın Komşu algoritmasının eğitim aşamasının anında bittiği, ancak büyük bir veri setinde test aşaması maliyetinin fazla olduğu görülmüştür. Karar Ağacı ise kuralları en baştan belirlediği için test aşamasında devasa boyutlardaki verilerde bile çok daha performanslı çalışmaktadır. Karar ağaçlarında `maxDepth` parametresinin modelin ezberleme oranına doğrudan etki ettiği görülmüş ve ağacın dengeli büyümesinin önemi anlaşılmıştır.

## 7. SONUÇ VE KARŞILAŞILAN ZORLUKLAR
Bu proje sayesinde temel makine öğrenmesi algoritmalarının hazır kütüphaneler olmaksızın Java diliyle nasıl kodlanacağı ve Nesneye Yönelik Programlama prensiplerinin kod organizasyonunu nasıl kolaylaştırdığı deneyimlenmiştir. 

Proje geliştirilirken karşılaşılan en büyük zorluklardan biri veri setindeki sütunlar arasındaki ölçek farkının KNN mesafesini bozması olmuştur. Bu durum bağımsız değişkenlerin [0-1] aralığına normalize edilmesiyle çözülmüştür. Ayrıca arayüz (GUI) programlamada uzun süren analiz işlemlerinin ana işlem iş parçacığını (EDT) dondurmasını engellemek için Java `SwingWorker` sınıfı kullanılmış ve arayüz asenkron çalışır hale getirilmiştir.

## 8. KAYNAKÇA
[1] S. Russell ve P. Norvig, *Artificial Intelligence: A Modern Approach*, 3rd ed., Prentice Hall, 2009.  
[2] J. R. Quinlan, "Induction of decision trees", *Machine Learning*, vol. 1, no. 1, pp. 81-106, 1986.  
[3] Oracle, "Concurrency in Swing", Java Documentation. [Çevrimiçi]. Erişim: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/  
[4] T. Mitchell, *Machine Learning*, McGraw-Hill, 1997.  

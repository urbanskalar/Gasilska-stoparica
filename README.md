# Gasilska Štoparica

Sistem je bil razvit kot pomagalo pri treningih za pripravo na tekme v spajanju sesalnega voda. Sestavljata ga štoparica v obliki aplikacije, ki se jo poganja na računalniku ter krmilniško vezje, ki komunicira z računalnikom in preverja stanje tipk za ustavitev štoparice.  

<p align="center">
  <img width="500px" height="auto" src="https://github.com/urbanskalar/Gasilska-stoparica/blob/main/05%20slike/blok%20diagram.png">
</p>  

Aplikacija je spisana v javi, za njeno delovanje pa je potrebno imeti nameščeno JDK/JRE 1.8. Sam sem uporabljal ```java version "1.8.0_261"```, z ostalimi različicami Jave ni bilo stestirano in morda ne bo delovalo. Če želite preveriti katero verzijo jave imate nameščeno, lahko to storite z ukazom terminalu/cmd ```java -version```. Aplikacijo se zažene z dvojnim klikom na ```./01 Stopwatch/dist/TikTakApp.jar``` ali z ukazom v terminalu/cmd ```java -jar ./01 Stopwatch/dist/TikTakApp.jar```.  

Štoparico se lahko štarta, ustavi in resetira preko gumba na dnu okna. Do njega obstaja tudi bližnjica preko tipke ```Alt Gr``` na tipkovnici. Aplikacija vsebuje tabelo za sledenje rezultatom na treningu. Vnose v tabeli je možno tudi brisati in izvoziti v obliki csv datoteke. Možno je tudi spremljanje vmesnih časov. Vmesni časi se shranjujejo ob pritisku tipke ```+``` na tipkovnici.  

<p align="center">
  <img src="https://github.com/urbanskalar/Gasilska-stoparica/blob/main/05%20slike/%C5%A1toparica.jpg">
</p>  

Pred uporabo aplikacije je potrebno urediti nastavitve. Do njih dostopamo preko gumba z ikono zobnika. Ta nam odpre novo okno, na katerem lahko nastavimo pot do avdio datoteke, ki se predvaja pred štartom štoparice, zamaknitev štarta štoparice glede na posnetek (če ima slučajno posnetek na koncu dolg premor), zakasnitev štarta štoparice ter nastavitve komunikacije z krmilniškim vezjem (COM port in baudrate).  

<p align="center">
  <img src="https://github.com/urbanskalar/Gasilska-stoparica/blob/main/05%20slike/nastavitve.jpg">
</p>

Koda za krmilniško vezje je spisana v okolju Arduino (c++). Vezje skrbi za branje tipk/stikal, ki so povezana na vhode vezja in pošilja računalniku id tipke, ki je bila pritisnjena. Program podpira uporabo NO in NC tipk/stikal. V prvem ciklu izvajanja programa krmilnik namreč prebere stanja tipk in tako ugotovi tip. Zato je pomembno da ob zagonu ni nobena tipka pritisnjena, drugače bo program zaznaval spust tipke namesto pritiska. Vhode na katerih želimo uporabljati tipke določimo preko spodnje spremenljivke.
```c++
 //define the buttons that we'll use.
 byte buttons[] = {2, 3, 4, 6, 7, 8, 10}; 
 ```
 
 

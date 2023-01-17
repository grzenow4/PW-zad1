# Zadanie 'Warsztaty'
Gospodarka współdzielenia (ang. sharing economy) to trend społeczno-ekonomiczny opierający się na dzieleniu się potencjalnie kosztownymi zasobami z innymi uczestnikami rynku, co w szczególności pozwala zamortyzować koszty utrzymania takich zasobów. Jednym z przykładów trendu są warsztaty wyposażone w wysokiej jakości specjalistyczne stanowiska. Urządzenia tworzące takie stanowiska niejednokrotnie mogą być poza zasięgiem finansowym przysłowiowego Kowalskiego. Zakładając jednak, że pojedynczy Kowalski nie potrzebuje ich na co dzień, lecz jedynie okazjonalnie, ekonomicznie uzasadnione jest ich współdzielenie, na przykład w ramach rozważanego warsztatu, którego użytkownikami jest wielu Kowalskich o podobnych potrzebach. Twoim zadaniem będzie rozwiązanie w języku Java problemu współbieżnej koordynacji dostępu użytkowników do stanowisk w takim warsztacie zgodnie z poniższymi wymaganiami. Do implementacji rozwiązania należy wykorzystać załączony szablon.

## Specyfikacja
W naszym modelu pojedynczy warsztat składa się z pewnej kolekcji stanowisk roboczych (obiektów klas dziedziczących po klasie [cp2022.base.Workplace](cp2022/base/Workplace.java) z załączonego szablonu). Każde stanowisko posiada unikalny w ramach warsztatu identyfikator (obiekt klasy dziedziczącej po klasie [cp2022.base.WorkplaceId](cp2022/base/WorkplaceId.java), dostępny poprzez metodę `getId` klasy [cp2022.base.Workplace](cp2022/base/Workplace.java)) oraz może być używane przez dowolnego użytkownika (metoda `use` klasy [cp2022.base.Workplace](cp2022/base/Workplace.java)).

Każdy potencjalny użytkownik warsztatu jest reprezentowany przez jeden wątek Javy i posiada unikalny identyfikator (metoda `getId` klasy `Thread`). Zbiór osób chcących skorzystać z warsztatu może być zatem dowolnie duży i może dynamicznie się zmieniać.

Warsztat (tj. obiekt implementujący poniższy interfejs [cp2022.base.Workshop](cp2022/base/Workshop.java)) jest odpowiedzialny za koordynację dostępu użytkowników do swoich stanowisk, jak wyjaśniamy dalej.
```Java
public interface Workshop {
    
    public Workplace enter(WorkplaceId wid);
    
    public Workplace switchTo(WorkplaceId wid);
    
    public void leave();
    
}
```
W systemie może działać wiele niezależnych warsztatów. Zbiory stanowisk je tworzących są parami rozłączne. W ciągu swojego życia pojedynczy użytkownik może korzystać z różnych warsztatów, ale w danym momencie tylko z jednego.

## Działanie pojedynczego użytkownika
Użytkownik wchodząc do warsztatu zajmuje konkretne stanowisko (metoda `enter` interfejsu `Workshop` zwracająca obiekt reprezentujący stanowisko o podanym jako parametr identyfikatorze) i rozpoczyna przy nim pracę (wywołując ww. metodę `use` na obiekcie je reprezentującym). Następnie może wykonać jedną z dwóch czynności: albo opuścić warsztat (metoda `leave` interfejsu `Workshop`), albo zmienić stanowisko pracy (metoda `switchTo` interfejsu `Workshop` zwracająca obiekt reprezentujący żądane stanowisko o identyfikatorze podanym jako parametr) i rozpocząć przy nim pracę (jak poprzednio, wywołując ww. metodę `use` na obiekcie je reprezentującym). Zanim opuści warsztat, użytkownik może zmieniać stanowiska pracy dowolną (skończoną) liczbę razy. W szczególności może wielokrotnie wracać do tego samego stanowiska, nawet tuż po zakończeniu przy nim pracy. Użytkownik może także dowolnie (potencjalnie nieskończenie) wiele razy pojawiać się w warsztacie.

Na potrzeby dalszych rozważań przyjmujemy następujące nazewnictwo. Użytkownik wchodzi do warsztatu w momencie, gdy wątek mu odpowiadający kończy wykonywanie metody `enter` zwracając obiekt pierwszego żądanego stanowiska. Analogicznie, użytkownik opuszcza warsztat w momencie, gdy odpowiadający wątek rozpoczyna wykonywanie metody `leave`. Pomiędzy tymi momentami, użytkownik przebywa w warsztacie. Natomiast użytkownik, który rozpoczął, ale nie zakończył w żaden sposób wywołania metody `enter` chce wejść do warsztatu. Podobnie, użytkownik zmienia stanowisko pracy w momencie, gdy odpowiadający mu wątek kończy wykonywanie metody `switchTo` zwracając obiekt kolejnego żądanego stanowiska. Pomiędzy momentem zakończenia metody `enter` lub `switchTo` dla danego stanowiska a rozpoczęciem wykonania metody `leave` lub zakończeniem wykonania metody `switchTo` dla innego stanowiska użytkownik przebywa przy danym stanowisku (zajmuje to stanowisko). Gdzieś tym okresie, w momencie wywołania metody `use` na obiekcie reprezentującym stanowisko użytkownik rozpoczyna pracę na stanowisku, zaś wraz z zakończeniem tej metody kończy pracę na stanowisku. Wreszcie, użytkownik, który rozpoczął, ale nie zakończył wywołania metody `switchTo` chce zmienić stanowisko pracy.

## Koordynacja użytkowników
Warsztat koordynuje dostęp różnych użytkowników do stanowisk zgodnie z poniższymi regułami.

Ze względów bezpieczeństwa użytkownik nie może pracować na stanowisku jeśli inny użytkownik je zajmuje, niezależnie czy tamten użytkownik przy nim pracuje, czy już/jeszcze nie. Twoje rozwiązanie musi bezwzględnie spełniać powyższy warunek.

Ze względów ekonomicznych ważne jest także jak najlepsze wykorzystanie stanowisk. Nie może ono jednak prowadzić do zagłodzenia którychkolwiek użytkowników. Innymi słowy, celem nadrzędnym jest maksymalizacja użycia stanowisk, ale (na potrzeby testowania przez nas) Twoje rozwiązanie musi zagwarantować, że użytkownik chcący wejść do warsztatu lub zmienić stanowisko pracy zrobi to zanim do tego warsztatu wejdzie `2*N` innych użytkowników, którzy zaczęli chcieć wejść po tym, gdy on zaczął chcieć, odpowiednio, wejść lub zmienić stanowisko (gdzie N to liczba stanowisk w warsztacie).

## Wymagania
Twoim zadaniem jest zaimplementowanie warsztatu według powyższej specyfikacji i dostarczonego szablonu przy wykorzystaniu mechanizmów współbieżności języka Java 11. Twój kod źródłowy powinien być napisany w zgodzie z dobrymi praktykami programistycznymi. Rozwiązania oparte na aktywnym lub półaktywnym (np. `sleep`, `yield` lub inne metody wykorzystujące ograniczenia czasowe) oczekiwaniu nie otrzymają żadnych punktów.

Dla uproszczenia rozwiązań zakładamy, że wątki odpowiadające użytkownikom nie są nigdy przerywane (tj. nigdy nie jest wołana dla nich metoda `interrupt` klasy `Thread`). Reakcją na pojawienie się wyjątku wynikającego z takiego przerwania (np. `InterruptedException` lub `BrokenBarrierException`) powinno być podniesienie wyjątku w następujący sposób:
```Java
throw new RuntimeException("panic: unexpected thread interruption");
```

Szczegółowe dalsze wymagania formalne są następujące.
1. Nie możesz w żaden sposób zmieniać zawartości pakietów [cp2022.base](cp2022/base) oraz [cp2022.demo](cp2022/demo).
2. Klasy implementujące rozwiązanie możesz dodawać jedynie w pakiecie [cp2022.solution](cp2022/solution), ale nie możesz tworzyć w tym pakiecie żadnych podpakietów.
3. Twoja implementacja nie może tworzyć żadnych wątków.
4. W klasie [cp2022.solution.WorkshopFactory](cp2022/solution/WorkshopFactory.java) musisz dodać treść metody `newWorkshop`, która będzie wykorzystywana do instancjonowania zaimplementowanego przez Ciebie warsztatu. Każde wywołanie tej metody powinno tworzyć nowy obiekt warsztatu. Wiele obiektów warsztatu powinno być w stanie działać w tym samym czasie. Nie wolno natomiast w żaden sposób zmieniać sygnatury tej metody ani nazwy klasy czy jej lokalizacji.
5. Wywołanie metod `enter` lub `switchTo` Twojej implementacji interfejsu [cp2022.base.Workshop](cp2022/base/Workshop.java) może zwrócić oryginalny obiekt stanowiska o żądanym identyfikatorze (tj. ten przekazany ww. metodzie `newWorkshop` klasy [cp2022.solution.WorkshopFactory](cp2022/solution/WorkshopFactory.java)) lub obiekt innej klasy dziedziczącej po klasie [cp2022.base.Workplace](cp2022/base/Workplace.java). Jednakże w tym drugim przypadku każde wywołanie metody `use` musi kiedyś doprowadzić do dokładnie jednego wywołania metody `use` oryginalnego obiektu stanowiska. Innymi słowy, zwrócony obiekt stanowiska może być co najwyżej dekoratorem obiektu oryginalnego a gwarancje bezpieczeństwa aplikują się do oryginalnego obiektu.
6. Możesz stworzyć sobie własne pakiety do testów, np. `cp2022.tests`, ale te pakiety będą ignorowane przy testowaniu przez nas, więc w szczególności kod Twojego warsztatu nie może od nich zależeć.
7. W plikach źródłowych Javy nie możesz używać nieanglojęzycznych znaków (w szczególności polskich znaków).
8. Twoje rozwiązanie powinno składać się z jednego pliku `ab123456.zip`, gdzie `ab123456` należy zastąpić swoim loginem z maszyny `students.mimuw.edu.pl` (będącym zwykle konkatenacją inicjałów i numeru indeksu). Plik ten musi mieć taką samą strukturę, jak szablon, to jest musi zawierać jedynie katalog [cp2022](cp2022) reprezentujący pakiet o tej samej nazwie, który zawiera katalogi odpowiednich podpakietów, co najmniej [base](cp2022/base), [demo](cp2022/demo) i [solution](cp2022/solution), które z kolei zawierają odpowiednie pliki źródłowe (*.java).
9. Twoje rozwiązanie musi kompilować się na maszynie `students.mimuw.edu.pl` poleceniem
```bash
javac cp2022/base/*.java cp2022/solution/*.java cp2022/demo/*.java
```
10. W Twoim rozwiązaniu musi działać program demonstracyjny, wywoływany poleceniem
```bash
java cp2022.demo.TroysWorkshop
```
to jest nie może on zgłaszać żadnych wyjątków.

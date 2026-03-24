# Weather App

Applicazione console Java per recuperare e visualizzare informazioni meteo in tempo reale per qualsiasi città del mondo.

## Scopo

L'applicazione permette di ottenere informazioni meteorologiche attuali (temperatura) per una città specificata dall'utente, utilizzando API esterne per il geocoding e i dati meteo.

## Requisiti

- Java 11 o superiore
- Maven 3.6 o superiore
- Connessione internet

## Installazione

1. Clona o scarica il progetto:
```bash
git clone <repository-url>
cd weather-app
```

2. Configura le variabili ambiente (opzionale):
```bash
cp settings.env.example settings.env
# Modifica settings.env con le tue configurazioni
```

3. Compila il progetto con Maven:
```bash
mvn clean compile
```

4. Installa le dipendenze:
```bash
mvn install
```

## Avvio dell'applicazione

### Metodo 1: Con Maven
```bash
mvn exec:java -Dexec.mainClass="com.dom.weatherapp.Main"
```

### Metodo 2: Dopo la compilazione
```bash
mvn package
java -cp target/weather-app-1.0-SNAPSHOT.jar com.dom.weatherapp.Main
```

## Come usare l'app

1. Avvia l'applicazione seguendo una delle modalità indicate sopra
2. Il programma ti chiederà di inserire il nome di una città
3. Digita il nome della città e premi Invio
4. L'app mostrerà la temperatura attuale per quella città

## Esempio di input

```
Inserisci una città: Roma
```

## Esempio di output

```
Meteo per Roma
Temperatura: 18.5°C
```

## Gestione degli errori

L'applicazione gestisce i seguenti casi di errore:

- **Città non trovata**: Se la città inserita non esiste o non viene trovata nel database di geocoding
  ```
  Errore: Citta 'NomeCittà' non trovata. Verificare il nome e riprovare.
  ```

- **Input non valido**: Se l'utente inserisce una stringa vuota, solo spazi, o input non validi (numeri, nomi troppo lunghi)
  ```
  Errore: Input non valido. Inserire un nome di città valido.
  ```

- **Problemi di connessione**: Se non è possibile connettersi alle API esterne
  ```
  Errore: Problema di connessione. Riprovare pi tardi.
  ```

- **Servizi non disponibili**: Se le API esterne non sono temporaneamente disponibili
  ```
  Errore: Servizio meteo non disponibile. Riprovare pi tardi.
  Errore: Servizio di geolocalizzazione non disponibile. Riprovare pi tardi.
  ```

- **Dati meteo non disponibili**: Se per una città trovata non sono disponibili dati meteo
  ```
  Errore: Dati meteo non disponibili per la citta'NomeCittà'.
  ```

- **Errori imprevisti**: Per qualsiasi altro tipo di errore
  ```
  Errore: Si è verificato un errore imprevisto. Riprovare pi tardi.
  ```

## Dipendenze principali

- Jackson (2.17.2): Per la gestione del JSON
- JUnit 5 (5.10.0): Per i test unitari
- Mockito (5.7.0): Per il mocking nei test

## Sicurezza

### Misure di sicurezza implementate:

1. **Validazione Input**: Tutti gli input utente sono validati e sanitizzati prima dell'uso
2. **Timeout HTTP**: Le richieste API hanno timeout configurati per prevenire attacchi DoS
3. **Cache Sicura**: La cache ha limiti di dimensione per prevenire esaurimento memoria
4. **Logging Sicuro**: I log non contengono informazioni sensibili e le credenziali sono mascherate
5. **Gestione Errori**: Gli errori non espongono stack trace o informazioni sensibili
6. **Configurazione Esterna**: Le configurazioni sensibili sono caricate da file esterni

### Best practice seguite:

- Nessuna credenziale hardcoded nel codice
- Utilizzo di Java 11+ con le ultime patch di sicurezza
- Dipendenze mantenute aggiornate alle versioni più recenti
- Validazione rigorosa degli input per prevenire injection
- Timeout appropriati per tutte le operazioni di rete

### Raccomandazioni per la produzione:

1. Utilizzare un file `.env` separato per le configurazioni sensibili
2. Monitorare i log di sicurezza per attività sospette
3. Mantenere le dipendenze aggiornate regolarmente
4. Configurare un firewall per limitare le chiamate API
5. Implementare rate limiting per le chiamate API

## Funzionalità avanzate

### Sistema di Cache
L'applicazione implementa un sistema di cache intelligente con le seguenti caratteristiche:

- **Cache in-memory**: Utilizza `ConcurrentHashMap` per storage thread-safe
- **Durata cache**: 30 minuti per ridurre le chiamate API
- **Dimensione massima**: 1000 entry per prevenire esaurimento memoria
- **Case-insensitive**: "Roma", "roma" e "ROMA" utilizzano la stessa cache
- **Auto-pulizia**: La cache viene svuotata automaticamente quando raggiunge il limite dimensione
- **Gestione scadenza**: I dati scaduti vengono automaticamente ricaricati dalle API

### Validazione Input Avanzata
L'applicazione include validazione rigorosa degli input:

- **Sanitizzazione**: Rimozione caratteri pericolosi e normalizzazione
- **Lunghezza massima**: Limitazione dei nomi città per prevenire attacchi
- **Controllo caratteri**: Verifica che i nomi contengano solo caratteri validi
- **Gestione spazi**: Trim automatico e validazione di input vuoti

### Gestione Errori Robusta
- **Optional Pattern**: Utilizzo di `Optional<WeatherData>` per gestire assenza di dati
- **Timeout configurati**: 15 secondi per chiamate API, 10 secondi per connessioni
- **Retry implicito**: Cache miss automatico con nuova chiamata API
- **Logging sicuro**: Errori loggati senza esporre informazioni sensibili

## Architettura

L'applicazione segue un'architettura MVC (Model-View-Controller):

- **Model**: `WeatherData` - Rappresenta i dati meteo
- **View**: `ConsoleView` - Gestisce l'interfaccia a riga di comando
- **Controller**: `WeatherController` - Coordina le operazioni
- **Service**: `WeatherService` - Contiene la logica di business
- **Client**: `WeatherApiClient` - Gestisce le chiamate alle API esterne
- **Config**: `ApiConfig` - Gestisce la configurazione centralizzata
- **Util**: `InputValidator`, `SecureLogger` - Utilità per sicurezza e validazione

## Testing

L'applicazione include una suite completa di test unitari con le seguenti categorie:

### Test di Funzionalità Base
- **Input validi**: Verifica il corretto funzionamento con città valide
- **Input non validi**: Testa gestione di input vuoti, null, con numeri, troppo lunghi
- **Case sensitivity**: Verifica che "Roma", "roma", "ROMA" funzionino correttamente

### Test di Cache
- **Cache hit**: Verifica che dati in cache vengano restituiti senza chiamate API
- **Cache miss**: Assicura che città diverse generino chiamate API separate
- **Cache expiration**: Testa il ricaricamento dati dopo scadenza cache
- **Cache case-insensitive**: Verifica che diverse capitalizzazioni usino stessa cache
- **Cache consistency**: Testa coerenza timestamp tra chiamate in cache

### Test di Robustezza
- **Timeout**: Simula timeout di connessione
- **Errori API**: Testa gestione di risposte HTTP non-200
- **Parsing error**: Verifica gestione di JSON malformati
- **Concorrenza**: Testa accesso simultaneo alla cache

### Esecuzione Test
```bash
# Esegui tutti i test
mvn test

# Esegui solo test compilation
mvn test-compile

# Esegui test con report coverage
mvn test jacoco:report
```

## API utilizzate

- **Open-Meteo Geocoding API**: Per convertire nomi di città in coordinate lat/lon
- **Open-Meteo Weather API**: Per ottenere i dati meteo attuali
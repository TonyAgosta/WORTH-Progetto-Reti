# WORTH
Il progetto è focalizzato sull'organizzazione e la gestione di progetti in modo collaborativo. Le apllicazioni di collaborazione e project mangement aiutano le persone a organizzarsi e coordinarsi nello svolgimento di progetti comuni. Questi possono essere progetti professionali, o in generale qualsiasi attività possa essere organizzata in una serie di compiti che sono svolti da membri di un gruppo: le applicazioni di interesse sono di diverso tipo, si pensi all'organizzazione di un progetto di sviluppo software con i colleghi del team di sviluppo.
Lo scopo del progetto è l'implementazione del metodo Kanban, un metodo di gestione "agile". La lavagna Kanban fornisce una vista di insieme delle attività e ne visualizza l'evoluzione, ad esmpio dalla creazione e il successivo progesso fino al completamento, dopo che è stata superata con successo la revisione. Una persona del gryppo di lavoro può prendere in carico un'attività quando ne ha la possibilità, spostando l'attività sulla lavagna.
Il progetto consinste nell'implementazione di uno strumento per la gestione di progetti collaborativi che si ispira ad alcuni principi della metodologia Kanban.
# Specifica delle operazioni
Gli utenti possono accedere a WORTH dopo registrazione e login.
In WORTH, un progetto, identificato da un nome univoco, è costituito da una serie di “card” (“carte”), che rappresentano i compiti da svolgere per portarlo a termine, e fornisce una serie di servizi. Ad ogni progetto è associata una lista di membri, ovvero utenti che hanno i permessi per modificare le card e accedere ai servizi associati al progetto (es. chat).

Una card è composta da un nome e una descrizione testuale. Il nome assegnato alla card deve essere univoco nell’ambito di un progetto. Ogni progetto ha associate quattro liste che definiscono il flusso di lavoro come passaggio delle card da una lista alla successiva: TODO, INPROGRESS, TOBEREVISED, DONE. Qualsiasi membro del progetto può spostare la card da una lista all’altro. 

Le card appena create sono automaticamente inserite nella lista TODO. Qualsiasi membro può spostare una card da una lista all’altra. Quando tutte le card sono nella lista DONE il progetto può essere cancellato, da un qualsiasi membro partecipante al progetto. 

Ad ogni progetto è associata una chat di gruppo, e tutti i membri di quel progetto, se online (dopo aver effettuato il login), possono ricevere i messaggi inviati sulla chat. Sulla chat il sistema invia inoltre automaticamente le notifiche di eventi legati allo spostamento di una card del progetto da una lista all’altra.
Un utente registrato e dopo login eseguita con successo ha i permessi per:

  ● recuperare la lista di tutti gli utenti registrati al servizio;
  
  ● recuperare la lista di tutti gli utenti registrati al servizio e collegati al servizio (in stato online); 
  
  ● creare un progetto;
  
  ●recuperare la lista dei progetti di cui è membro.
  
Un utente che ha creato un progetto ne diventa automaticamente membro. Può aggiungere altri utenti registrati come membri del progetto. Tutti i membri del progetto hanno gli stessi diritti (il creatore stesso è un membro come gli altri), in particolare:

● aggiungere altri utenti registrati come membri del progetto;

● recuperare la lista dei membri del progetto;

● creare card nel progetto;

● recuperare la lista di card associate ad un progetto;

● recuperare le informazioni di una specifica card del progetto;

● recuperare la “storia” di una specifica card del progetto;

● spostare qualsiasi card del progetto;

● inviare un messaggio sulla chat di progetto;

● leggere messaggi dalla chat di gruppo;

● cancellare il progetto.

# Specifiche per l'implementazione
● la fase di registrazione è implementata mediante RMI.
● La fase di login deve essere effettuata come prima operazione dopo aver instaurato una connessione TCP con il server. In risposta all’operazione di login, il server invia anche la lista degli utenti registrati e il loro stato (online, offline). A seguito della login il client si registra ad un servizio di notifica del server per ricevere aggiornamenti sullo stato degli utenti registrati (online/offline). Il servizio di notifica è implementato con il meccanismo di RMI callback. Il client mantiene una struttura dati per tenere traccia della lista degli utenti registrati e il loro stato (online/offline), la lista viene quindi aggiornata a seguito della ricezione di una callback (attraverso la quale il server manda gli aggiornamenti).
● dopo previa login effettuata con successo, l’utente interagisce, secondo il modello client-server (richieste/risposte), con il server sulla connessione TCP creata, inviando i comandi elencati in precedenza. Tutte le operazioni sono effettuate su questa connessione TCP, eccetto la registrazione (RMI), le operazioni di visualizzazione della lista degli utenti (listUsers e listOnlineusers) che usano la struttura dati locale del client aggiornata tramite il meccanismo di RMI callback (come descritto al punto precedente) e le operazioni sulla chat.
● Il server effettua il multiplexing dei canali mediante NIO.
● L'utente interagisce con WORTH mediante un client che può utilizza una interfaccia a linea di comando, definendo un insieme di comandi, presentati in un menu.
● La chat di progetto è realizzata usando UDP multicast (un client può inviare direttamente i messaggi ad altri client).
● Il server persiste lo stato del sistema, in particolare: le informazioni di registrazione, la lista dei progetti (inclusi membri, card e lo stato delle liste). Lo stato dei progetti è stato reso persistente sul file system come descritto di seguito: una directory per ogni progetto e un file per ogni card del progetto (sul file sono accodati gli eventi di spostamento relativi alla card). Quando il server viene riavviato tali informazioni sono utilizzate per ricostruire lo stato del sistema.

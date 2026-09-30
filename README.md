# AutoChat WhatsApp — prototype v0.1

Prototype Android gratuit basé sur Kotlin + Jetpack Compose + WorkManager.

Fonctions actuelles :
- saisie d'un numéro WhatsApp international ;
- rédaction d'un message ;
- propositions de réponses locales de démonstration ;
- ouverture de WhatsApp avec message prérempli via `wa.me` ;
- programmation locale d'une tâche.

Limite : cette version ne prend pas le contrôle de WhatsApp et n'envoie pas silencieusement des messages dans l'application WhatsApp personnelle.

Étape suivante : connecter un modèle IA local (par exemple via Ollama sur PC ou un runtime local Android), ajouter le profil de style et la mémoire conversationnelle, puis étudier les intégrations WhatsApp officiellement disponibles.

# 🤝 HandHelp — Application de Gestion du Bénévolat

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

**Ensemble, on fait la différence.**

</div>

---

## 📱 Présentation

**HandHelp** est une application mobile Android moderne dédiée à la gestion du bénévolat. Elle met en relation des **bénévoles** avec des **organisateurs** de missions solidaires, permettant à chacun de contribuer positivement à sa communauté.

---

## ✨ Fonctionnalités

### 👤 Authentification
- ✅ Inscription / Connexion Email & Mot de passe
- ✅ Connexion avec Google Sign-In
- ✅ Réinitialisation du mot de passe
- ✅ Choix du rôle à l'inscription (Bénévole ou Organisateur)
- ✅ Déconnexion sécurisée

### 🙋 Espace Bénévole
- ✅ Accueil avec liste des missions disponibles en temps réel
- ✅ Filtrage des missions par catégorie
- ✅ Recherche de missions par mot-clé
- ✅ Détail d'une mission avec barre de progression
- ✅ Inscription / Désinscription à une mission
- ✅ Historique des missions participées
- ✅ Notifications en temps réel

### 🏢 Espace Organisateur
- ✅ Tableau de bord avec statistiques en temps réel
- ✅ Création de missions avec formulaire complet
- ✅ Gestion des missions (terminer / supprimer)
- ✅ Suivi du nombre de bénévoles inscrits
- ✅ Notifications automatiques (nouveau bénévole, mission complète)

### 👤 Profil
- ✅ Avatar avec initiale du nom (généré automatiquement)
- ✅ Modification du profil (nom, téléphone, bio)
- ✅ Statistiques personnalisées selon le rôle
- ✅ Sécurité : changement de mot de passe + désactivation du compte
- ✅ Aide & Support : FAQ interactive + contact email

---

## 🛠️ Technologies utilisées

| Technologie | Usage |
|---|---|
| **Kotlin** | Langage principal |
| **Jetpack Compose** | Interface utilisateur moderne |
| **Material Design 3** | Design system |
| **Firebase Authentication** | Authentification utilisateurs |
| **Firebase Firestore** | Base de données temps réel |
| **Firebase Cloud Messaging** | Notifications push |
| **Hilt** | Injection de dépendances |
| **Navigation Compose** | Navigation entre écrans |
| **Coroutines + Flow** | Programmation asynchrone |
| **MVVM** | Architecture |

---

## 🏗️ Architecture

L'application suit le pattern **MVVM (Model-View-ViewModel)** avec une séparation claire des responsabilités :

```
app/
└── src/main/java/com/example/handhelp/
    ├── data/
    │   └── model/              # Modèles de données (User, Mission, Notification)
    ├── repository/             # Couche d'accès aux données (Firestore, Auth)
    ├── viewmodel/              # Logique métier et états UI
    ├── navigation/             # Routes et navigation Compose
    ├── service/                # Firebase Messaging Service
    ├── di/                     # Modules d'injection Hilt
    ├── ui/
    │   ├── theme/              # Couleurs, typographie, thème
    │   ├── components/         # Composants réutilisables
    │   └── screens/            # Écrans de l'application
    │       ├── auth/           # Login, Register, ForgotPassword
    │       ├── role/           # Sélection du rôle
    │       ├── volunteer/      # Écrans bénévole
    │       └── organizer/      # Écrans organisateur
    ├── utils/                  # Constantes et utilitaires
    ├── MainActivity.kt
    └── HandHelpApplication.kt
```

### Flux de données

```
UI (Screen)
    ↕
ViewModel         ← StateFlow / collectAsState()
    ↕
Repository        ← suspend functions / Flow
    ↕
Firebase          ← Firestore / Auth / FCM
```

---

## 🚀 Installation

### Prérequis
- Android Studio Hedgehog ou supérieur
- JDK 11
- Android SDK API 26+
- Compte Firebase

### Étapes

**1. Cloner le projet**
```bash
git clone https://github.com/ton-username/handhelp.git
cd handhelp
```

**2. Configurer Firebase**
- Créer un projet sur [console.firebase.google.com](https://console.firebase.google.com)
- Ajouter une application Android avec le package `com.example.handhelp`
- Télécharger `google-services.json` et le placer dans `/app/`
- Activer **Authentication** (Email/Password + Google)
- Activer **Firestore Database**
- Activer **Cloud Messaging**

**3. Ajouter le SHA-1**
```bash
./gradlew signingReport
```
Copier le SHA-1 et l'ajouter dans Firebase Console → Paramètres du projet.

**4. Configurer Google Sign-In**

Dans `AuthViewModel.kt`, remplacer :
```kotlin
.requestIdToken("VOTRE_WEB_CLIENT_ID")
```
Par le Web Client ID récupéré dans Firebase Console → Authentication → Google.

**5. Lancer l'application**

Ouvrir le projet dans Android Studio et lancer sur un émulateur ou appareil physique.

---

## 🗄️ Structure Firestore

```
firestore/
├── users/
│   └── {uid}/
│       ├── uid: String
│       ├── email: String
│       ├── displayName: String
│       ├── role: String (VOLUNTEER | ORGANIZER)
│       ├── phone: String
│       ├── bio: String
│       ├── fcmToken: String
│       └── createdAt: Long
│
├── missions/
│   └── {missionId}/
│       ├── id: String
│       ├── title: String
│       ├── description: String
│       ├── category: String
│       ├── location: String
│       ├── date: String
│       ├── time: String
│       ├── volunteersNeeded: Int
│       ├── volunteersEnrolled: Int
│       ├── organizerId: String
│       ├── organizerName: String
│       ├── participants: List<String>
│       ├── status: String (ACTIVE | COMPLETED | CANCELLED)
│       └── createdAt: Long
│
└── notifications/
    └── {notifId}/
        ├── id: String
        ├── userId: String
        ├── title: String
        ├── body: String
        ├── type: String
        ├── missionId: String
        ├── isRead: Boolean
        └── createdAt: Long
```

---

## 🔐 Règles de sécurité Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{userId} {
      allow read, write: if request.auth != null
        && request.auth.uid == userId;
    }

    match /missions/{missionId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null
        && request.resource.data.organizerId == request.auth.uid;
      allow update: if request.auth != null && (
        resource.data.organizerId == request.auth.uid ||
        request.resource.data.diff(resource.data).affectedKeys()
          .hasOnly(['participants', 'volunteersEnrolled'])
      );
      allow delete: if request.auth != null
        && resource.data.organizerId == request.auth.uid;
    }

    match /notifications/{notifId} {
      allow read, update, delete: if request.auth != null
        && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null;
    }
  }
}
```

---

## 📊 Index Firestore requis

| Collection | Champs | Ordre |
|---|---|---|
| `notifications` | `userId` ↑, `createdAt` ↓ | Collection |
| `notifications` | `userId` ↑, `isRead` ↑ | Collection |
| `missions` | `status` ↑, `createdAt` ↓ | Collection |
| `missions` | `organizerId` ↑, `createdAt` ↓ | Collection |
| `missions` | `participants` (array), `createdAt` ↓ | Collection |

---


## 📋 Écrans de l'application

| Écran | Description |
|---|---|
| `SplashScreen` | Écran de démarrage avec animation |
| `WelcomeScreen` | Page d'accueil avec choix connexion/inscription |
| `LoginScreen` | Connexion Email + Google |
| `RegisterScreen` | Inscription avec validation |
| `ForgotPasswordScreen` | Réinitialisation mot de passe |
| `RoleSelectionScreen` | Choix du rôle après inscription |
| `VolunteerHomeScreen` | Accueil bénévole avec missions |
| `MissionDetailScreen` | Détail + inscription à une mission |
| `SearchScreen` | Recherche de missions |
| `HistoryScreen` | Historique des participations |
| `OrganizerHomeScreen` | Tableau de bord organisateur |
| `AddMissionScreen` | Formulaire création de mission |
| `NotificationsScreen` | Centre de notifications |
| `ProfileScreen` | Profil utilisateur |
| `EditProfileScreen` | Modification du profil |
| `SecurityScreen` | Changement MDP + désactivation compte |
| `HelpSupportScreen` | FAQ + contact support |

---

## 👥 Rôles utilisateurs

### 🙋 Bénévole
- Consulter et rechercher des missions
- S'inscrire / se désinscrire d'une mission
- Voir son historique de participation
- Recevoir des notifications
- Gérer son profil

### 🏢 Organisateur
- Créer et gérer des missions
- Suivre les inscriptions des bénévoles
- Marquer les missions comme terminées
- Recevoir des notifications (nouveau bénévole, mission complète)
- Gérer son profil

---

## 🎨 Design System

| Élément | Valeur |
|---|---|
| **Couleur principale** | `#2E7D32` (Vert) |
| **Couleur secondaire** | `#4CAF50` (Vert clair) |
| **Couleur accent** | `#FF6F00` (Orange) |
| **Fond** | `#F5F5F5` |
| **Police** | Material Default |
| **Thème** | Material Design 3 |
| **Coins arrondis** | 12dp — 16dp |

---

## 🔮 Prochaines fonctionnalités

- [ ] Messagerie directe entre bénévoles et organisateurs
- [ ] Carte interactive des missions (Google Maps)
- [ ] Système de badges et récompenses
- [ ] Rappels automatiques (WorkManager)
- [ ] Mode hors ligne (cache local)
- [ ] Partage de missions sur les réseaux sociaux
- [ ] Statistiques avancées pour les organisateurs

---

## 👨‍💻 Développeur

**Soufiane Mouaddine**
**Ahmed Hourri**

Projet développé dans le cadre d'un cours Android — Architecture MVVM + Firebase.



---

<div align="center">
  <strong>HandHelp — Ensemble, on fait la différence. 🤝</strong>
</div>

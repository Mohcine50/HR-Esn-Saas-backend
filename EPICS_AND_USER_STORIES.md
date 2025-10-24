# 🧭 EPICS & USER STORIES — SaaS RH & Commercial Backend

## 📘 Contexte du projet

Le projet consiste à développer un **SaaS RH & Commercial** destiné aux **ESN et cabinets de conseil**.  
Il couvre la **gestion des missions**, **suivi des temps (timesheets)**, **facturation automatique**, **reporting**, **gestion des collaborateurs** et **administration**.  
L’objectif est de centraliser les processus RH et commerciaux au sein d’une seule plateforme.

---

## 🗂️ EPIC 1 — Gestion des Collaborateurs (Employee Management)

### 🎯 Objectif :
Gérer le cycle de vie complet d’un collaborateur dans le système.

### 🧩 User Stories :

1. **US1.1 - Ajouter un collaborateur**
    - En tant qu’admin RH, je veux ajouter un nouveau collaborateur avec ses informations personnelles (nom, email, poste, date d’embauche, type de contrat, TJM, etc.).
    - **Critères d’acceptation :**
        - Tous les champs obligatoires doivent être validés.
        - Un email de bienvenue est envoyé automatiquement.

2. **US1.2 - Consulter la liste des collaborateurs**
    - En tant qu’admin ou manager, je veux afficher la liste des collaborateurs avec filtres (statut, poste, client, mission active).
    - **Critères d’acceptation :**
        - Filtrage et pagination.
        - Affichage des missions actives pour chaque collaborateur.

3. **US1.3 - Modifier / désactiver un collaborateur**
    - En tant qu’admin RH, je veux modifier les informations d’un collaborateur ou le désactiver lorsqu’il quitte l’entreprise.

4. **US1.4 - Gestion des rôles et permissions**
    - En tant qu’administrateur, je veux attribuer un rôle (Admin, RH, Manager, Consultant, Comptable) pour contrôler l’accès.

---

## 🗂️ EPIC 2 — Gestion des Clients

### 🎯 Objectif :
Gérer les comptes clients, leurs contacts, et leurs contrats-cadres.

### 🧩 User Stories :

1. **US2.1 - Créer un client**
    - En tant que commercial, je veux enregistrer un nouveau client avec ses coordonnées et le nom du contact principal.

2. **US2.2 - Associer des missions à un client**
    - En tant que RH ou commercial, je veux lier une mission à un client pour centraliser les informations.

3. **US2.3 - Modifier/Supprimer un client**
    - En tant qu’administrateur, je veux pouvoir mettre à jour les informations d’un client.

4. **US2.4 - Consulter les statistiques client**
    - En tant que commercial, je veux voir les chiffres clés (CA, nombre de missions, taux de facturation, TJM moyen).

---

## 🗂️ EPIC 3 — Gestion des Missions

### 🎯 Objectif :
Gérer les missions (contrats entre collaborateur et client).

### 🧩 User Stories :

1. **US3.1 - Créer une mission**
    - En tant que RH ou commercial, je veux créer une mission avec les détails (client, collaborateur, date début/fin, TJM, type de mission).

2. **US3.2 - Suivi des missions actives**
    - En tant que manager, je veux voir les missions actives et leur état (en cours, à venir, terminée).

3. **US3.3 - Clôturer une mission**
    - En tant que RH, je veux clôturer une mission et archiver ses données.

4. **US3.4 - Générer le contrat PDF**
    - En tant que RH, je veux générer automatiquement le contrat de mission en PDF à partir d’un template.

---

## 🗂️ EPIC 4 — Gestion des Timesheets

### 🎯 Objectif :
Permettre aux collaborateurs de saisir leurs temps de travail sur leurs missions.

### 🧩 User Stories :

1. **US4.1 - Saisie du temps**
    - En tant que consultant, je veux saisir mes heures travaillées par jour et mission.
    - **Critères d’acceptation :**
        - Validation des jours travaillés dans la période de mission.
        - Blocage après validation par manager.

2. **US4.2 - Validation du timesheet**
    - En tant que manager, je veux valider ou rejeter les feuilles de temps soumises.

3. **US4.3 - Historique des timesheets**
    - En tant que collaborateur, je veux consulter mes saisies précédentes.

4. **US4.4 - Exportation mensuelle**
    - En tant que RH, je veux exporter les timesheets validés du mois pour la facturation.

---

## 🗂️ EPIC 5 — Facturation & Paiement

### 🎯 Objectif :
Automatiser la génération des factures basées sur les timesheets validés.

### 🧩 User Stories :

1. **US5.1 - Génération automatique des factures**
    - En tant que comptable, je veux générer automatiquement des factures pour les clients à partir des timesheets validés.

2. **US5.2 - Gestion du statut de facture**
    - En tant que comptable, je veux suivre les statuts (brouillon, envoyé, payé, en retard).

3. **US5.3 - Envoi automatique de facture par email**
    - En tant que comptable, je veux envoyer automatiquement les factures PDF aux clients.

4. **US5.4 - Intégration comptable**
    - En tant qu’admin, je veux exporter les données comptables vers un outil externe (Sage, QuickBooks...).

---

## 🗂️ EPIC 6 — Reporting & Analytics

### 🎯 Objectif :
Fournir des tableaux de bord décisionnels RH et commerciaux.

### 🧩 User Stories :

1. **US6.1 - Dashboard général**
    - En tant qu’administrateur, je veux un dashboard avec les KPIs clés (CA total, TJM moyen, taux de facturation, marge brute).

2. **US6.2 - Reporting RH**
    - En tant que RH, je veux des rapports sur l’effectif, l’absentéisme et les TJM par profil.

3. **US6.3 - Reporting commercial**
    - En tant que commercial, je veux suivre les performances clients et les revenus générés.

4. **US6.4 - Export des rapports**
    - En tant qu’utilisateur, je veux exporter les rapports en PDF ou Excel.

---

## 🗂️ EPIC 7 — Authentification & Sécurité

### 🎯 Objectif :
Gérer les accès et la sécurité des données via un système d’authentification robuste.

### 🧩 User Stories :

1. **US7.1 - Authentification par JWT**
    - En tant qu’utilisateur, je veux m’authentifier via un login/mot de passe et obtenir un token JWT.

2. **US7.2 - Rôles et autorisations**
    - En tant qu’administrateur, je veux contrôler l’accès aux routes API via des rôles.

3. **US7.3 - Réinitialisation de mot de passe**
    - En tant qu’utilisateur, je veux recevoir un lien de réinitialisation de mot de passe par email.

4. **US7.4 - Audit & Logs**
    - En tant qu’admin, je veux consulter les journaux d’accès et d’actions sensibles.

---

## 🗂️ EPIC 8 — Administration du Système

### 🎯 Objectif :
Gérer les configurations globales du système.

### 🧩 User Stories :

1. **US8.1 - Configuration générale**
    - En tant qu’administrateur, je veux définir les paramètres du système (devise, TVA, modèle de facture, etc.).

2. **US8.2 - Gestion des templates**
    - En tant qu’admin, je veux modifier les templates des contrats, factures, et emails.

3. **US8.3 - Sauvegarde et restauration**
    - En tant qu’admin, je veux planifier des sauvegardes automatiques de la base de données.

---

## 🗂️ EPIC 9 — Notifications & Communication

### 🎯 Objectif :
Informer les utilisateurs des événements clés.

### 🧩 User Stories :

1. **US9.1 - Notifications internes**
    - En tant qu’utilisateur, je veux recevoir des notifications dans le dashboard (validation timesheet, nouvelle mission...).

2. **US9.2 - Emails automatiques**
    - En tant que système, je veux envoyer des emails pour les actions clés (facture, réinitialisation, approbation...).

3. **US9.3 - Centre de notifications**
    - En tant qu’utilisateur, je veux un historique de mes notifications.

---

## 🗂️ EPIC 10 — Multitenancy (optionnel)

### 🎯 Objectif :
Permettre à plusieurs entreprises (ESN) d’utiliser la plateforme avec isolation des données.

### 🧩 User Stories :

1. **US10.1 - Gestion des espaces d’entreprise**
    - En tant qu’admin super, je veux créer et configurer un espace entreprise avec sa propre base logique.

2. **US10.2 - Isolation des données**
    - En tant que système, je veux garantir qu’un utilisateur ne peut voir que les données de son entreprise.

3. **US10.3 - Branding entreprise**
    - En tant qu’entreprise, je veux personnaliser le logo et le thème de mon espace.

---

## 📅 Notes de Gestion Agile

- **Sprints** : 2 semaines
- **Rôles** : Product Owner (PO), Scrum Master, Dev Backend, Dev Frontend, QA
- **Outils recommandés** :
    - Jira / Linear / Notion pour la gestion agile
    - GitHub / GitLab pour CI/CD
    - SonarQube pour la qualité de code
    - Postman pour la documentation API

---

## ✅ Conclusion

Ce backlog fonctionnel permet de couvrir l’ensemble des besoins d’un SaaS RH & Commercial complet dans un **monolithe Spring Boot**.  
Il peut évoluer ultérieurement vers une architecture microservices selon la croissance du produit.

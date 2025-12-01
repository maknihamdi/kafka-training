# Configuration IntelliJ IDEA

## ⚠️ Problème: java.lang.ExceptionInInitializerError com.sun.tools.javac.code.TypeTag

Ce problème survient lorsque IntelliJ utilise Java 24 alors que le projet nécessite Java 17.

## ✅ Solution: Configurer Java 17 dans IntelliJ

### Option 1: Configurer le JDK du projet

1. **Ouvrir les paramètres du projet**
   - Menu: `File` → `Project Structure` (ou `Cmd + ;` sur Mac, `Ctrl + Alt + Shift + S` sur Windows)

2. **Configurer le Project SDK**
   - Onglet `Project`
   - `SDK`: Sélectionnez **Java 17** (ou téléchargez-le via `Add SDK` → `Download JDK`)
   - `Language level`: Sélectionnez **17**
   - Cliquez sur `Apply`

3. **Configurer les Modules**
   - Onglet `Modules`
   - Sélectionnez chaque module (producer, consumer)
   - `Language level`: **17 - Sealed types, always-strict floating-point semantics**
   - Cliquez sur `Apply`

### Option 2: Configurer Maven dans IntelliJ

1. **Ouvrir les paramètres Maven**
   - Menu: `File` → `Settings` (ou `Cmd + ,` sur Mac)
   - `Build, Execution, Deployment` → `Build Tools` → `Maven`

2. **Configurer le JDK pour Maven**
   - `Maven home path`: Utilisez le Maven par défaut
   - `JDK for importer`: Sélectionnez **Java 17**
   - Cochez `Override` si nécessaire

3. **Reimporter le projet Maven**
   - Clic droit sur le `pom.xml` racine
   - `Maven` → `Reload Project`

### Option 3: Via le fichier .idea (automatique)

Le projet contient déjà la configuration Java 17 dans:
- `maven-compiler-plugin` configuré pour Java 17
- `java.version` = 17 dans les properties

Il suffit de:
1. **Fermer IntelliJ**
2. **Supprimer le dossier `.idea`** à la racine du projet
3. **Rouvrir le projet** dans IntelliJ
4. IntelliJ va détecter automatiquement Maven et configurer Java 17

### Vérification

Après configuration, vérifiez:

**Dans le terminal IntelliJ:**
```bash
mvn clean compile
```

Devrait afficher:
```
BUILD SUCCESS
```

**Lancer l'application:**
- Clic droit sur `ProducerApplication.java`
- `Run 'ProducerApplication'`
- L'application devrait démarrer sur le port 8081

## 📥 Installer Java 17

Si vous n'avez pas Java 17 installé:

### Via SDKMAN (recommandé)
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
```

### Via Homebrew (Mac)
```bash
brew install openjdk@17
```

### Via le site officiel
- [Adoptium (Eclipse Temurin)](https://adoptium.net/temurin/releases/?version=17)
- [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

## 🔍 Vérifier la version Java

```bash
java -version
```

Devrait afficher:
```
openjdk version "17.0.x" ...
```

## 🚀 Après configuration

Une fois Java 17 configuré:

1. **Nettoyer et recompiler:**
   ```bash
   mvn clean install
   ```

2. **Lancer le Producer:**
   - Run `ProducerApplication` dans IntelliJ
   - Ou: `cd producer && mvn spring-boot:run`

3. **Lancer le Consumer:**
   - Run `ConsumerApplication` dans IntelliJ
   - Ou: `cd consumer && mvn spring-boot:run`

## ⚙️ Configuration additionnelle (optionnel)

### Enable annotation processing (si vous réactivez Lombok plus tard)

1. `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
2. Cochez `Enable annotation processing`
3. `Apply` et `OK`

---

**Note:** Ce projet utilise Spring Boot 3.2.0 qui nécessite Java 17 minimum. Java 24 n'est pas encore officiellement supporté par Spring Boot 3.2.x.

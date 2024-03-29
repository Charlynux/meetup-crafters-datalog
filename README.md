## Description

Lors du précédent Meetup, nous avions joué avec le langage Prolog. Cette fois, nous vous proposons de continuer avec son petit frère Datalog.

Datalog est un langage de requête, qui s'appuie sur les mêmes concepts que Prolog. Vous rédigez votre requête avec des "trous" et lui se charge de les combler avec les informations de la base de données.

Datomic et Datascript sont deux bases de données, qui utilisent Datalog pour leurs requêtes.

Datomic est une base de données "immutable", inventée par Rich Hickey le créateur de Clojure.

Datascript est une base de données "in memory" prévue pour tourner dans le navigateur, mais qui peut aussi fonctionner dans la JVM.

Lors de ces dernières semaines, j'ai regardé quelques talks sur le sujet et j'ai joué un peu avec ces technologies. Cette soirée sera l'occasion de faire un tour d'horizon de ce que j'ai trouvé intéressant, puissant voire déroutant lors de mes expérimentations.

Si vous voulez découvrir une façon différente de penser la base de données, cette soirée devrait combler vos attentes.

## Local run

### Datascript

La base de données étant in-memory, il n'y a rien de spécial. Tout se fait directement dans le fichier `src/amiens_crafters/datascript.clj`.

### Datomic local

Désormais Datomic est disponible directement sur "Maven Central". Il n'y a donc plus d'actions manuelles pour disposer de la librairie.

Cette librairie permet de manipuler l'API "client" de Datomic avec une base de données "in-memory".

Le code se trouve dans `src/amiens_crafters/datomic_client.clj`.

### Datomic Pro

Pour tester toutes les possibilités de Datomic, il vous faudra installer Datomic et lancer 2 processus en parallèle. Le Makefile à la racine du projet simplifie cela.

- Télécharger le zip de Datomic Pro

``` shell
make local_install
```

- Lancer le transactor

``` shell
make run_transactor
```

- Lancer le peer server

``` shell
make run_client
```

#### Client Library

Dans le fichier `src/amiens_crafters/datomic_client.clj`, évaluer le `client` correspondant au type `:peer-server`.

#### Peer Library

Dans le fichier `src/amiens_crafters/datomic_peer.clj`, vous pourrez faire des manipulations au niveau des transactions. Pour cela, il faut avoir chargé la base de données avec les instructions de la partie Client.

### XTDB

Une autre base de données souvent citée comme alternative Open Source à Datomic.

Elle dispose d'un fonctionnement "in-memory", tout se fait donc dans le fichier
 `src/amiens_crafters/demo_xtdb`.

## Références

### Tutos & co

[Datomic Dev Local](https://docs.datomic.com/cloud/dev-local.html)

Comment obtenir le jar Datomic utilisé dans ce repository.

[Learn Datalog Today!](http://www.learndatalogtoday.org/)

Un rapide tutoriel pour apprendre Datalog.

[Querying accross Datomic databases](https://cjohansen.no/querying-across-datomic-databases/)

Un exemple élégant de requête multi-database s'appuyant sur `d/since`.

### Vidéos

Vous ne trouverez pas ici de talks de Rich Hickey au sujet de Datomic. J'ai préféré me concentrer sur des vidéos tournées autour de l'utilisation de Datomic plutôt que sur sa conception.

[Datomic Datalog - Stuart Halloway](https://youtu.be/bAilFQdaiHk)

Une présentation un peu formelle. Mais en 10 minutes, on fait le tour d'un grand nombre de fonctionnalités de Datomic.

[DOMAIN MODELING WITH DATALOG by Norbert Wojtowicz](https://youtu.be/oo-7mN9WXTw)
[Norbert Wojtowicz - Modeling your domain (Lambda Days 2016)](https://youtu.be/UrGJHfB21Ok)

Ces deux présentations sont données par la même personne. Elles contiennent des éléments similaires. La première est celle qui m'a le plus marqué.

[Lucas Cavalcanti & Edward Wible - Exploring four hidden superpowers of Datomic](https://youtu.be/7lm3K8zVOdY)

L'utilisation de Datomic chez Nubank (banque en ligne brésilienne). Pour information, en 2020, Nubank a racheté Cognitect la société qui développe Datomic.

[Alistair Roche - Datomic in Production](https://youtu.be/0rmR80neExo)

Un talk dynamique autour de "Datomic dans le monde réel".

[Yet & Datomic Immutable Facts Mutated Our Stack - Milton Reder](https://youtu.be/gcJmNYj4Mec)

Des réflexions et quelques cas d'usage de Datomic/Datascript et comment ces technologies ont changé la façon de penser d'une équipe.

[Simplifying ETL with Clojure and Datomic - Stuart Halloway](https://youtu.be/oOON--g1PyU)

La présentation la moins "Datomic" de la série. Elle est plutôt orientée Clojure.

## Alternative

[Datahike](https://github.com/replikativ/datahike)

"Port" de Datascript utilisant une autre structure de données en interne.

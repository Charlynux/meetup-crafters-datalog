## Description

Lors du précédent Meetup, nous avions joué avec le langage Prolog. Cette fois, nous vous proposons de continuer avec son petit frère Datalog.

Datalog est un langage de requête, qui s'appuie sur les mêmes concepts que Prolog. Vous rédigez votre requête avec des "trous" et lui se charge de les combler avec les informations de la base de données.

Datomic et Datascript sont deux bases de données, qui utilisent Datalog pour leurs requêtes.

Datomic est une base de données "immutable", inventée par Rich Hickey le créateur de Clojure.

Datascript est une base de données "in memory" prévue pour tourner dans le navigateur, mais qui peut aussi fonctionner dans la JVM.

Lors de ces dernières semaines, j'ai regardé quelques talks sur le sujet et j'ai joué un peu avec ces technologies. Cette soirée sera l'occasion de faire un tour d'horizon de ce que j'ai trouvé intéressant, puissant voire déroutant lors de mes expérimentations.

Si vous voulez découvrir une façon différente de penser la base de données, cette soirée devrait combler vos attentes.

## De quoi on parle ?

### Datomic ou Datascript

Commencer par Datascript, c'est être plus libre, puisque pas de schéma.

Datomic, c'est plus strict, mais c'est plus simple d'expliquer après qu'on "enlève" le schéma.

### Entity, Attribute, Value, Transaction, Operation

Introduction progressive 3, puis 4?, puis 5

### Datomic, immutabilité

- history
- as-of

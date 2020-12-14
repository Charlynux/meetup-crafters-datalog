(ns amiens-crafters.datomic-pro
  (:require [datomic.api :as d]))

(def db-uri "datomic:dev://localhost:4334/meetup-crafters-demo")

(comment
  (d/delete-database db-uri))
(d/create-database db-uri)

(def conn (d/connect db-uri))

;;;;;;
;; Requêtes autour des transactions
;;;;;

;; Quand l'information "role" a-t-elle été ajoutée ?
(d/q '[:find ?tx ?inst
       :where
       [_ :db/ident :employee/role ?tx]
       [?tx :db/txInstant ?inst]] (d/db conn))
;; => [[13194139533322 #inst "2020-12-04T18:12:33.732-00:00"]]

;; Quand a-t-elle été ajoutée la première fois ?
(d/q '[:find (min ?tx)
       :where
       [_ :employee/role _ ?tx]]
     (d/db conn))
;; => [[13194139533323]]

(def interesting-tx (ffirst *1))

(d/q '[:find ?e ?attr ?v ?op
       :in $ ?log ?tx
       :where
       [(tx-data ?log ?tx) [[?e ?a ?v _ ?op]]]
       [?a :db/ident ?attr]]
     (d/db conn) (d/log conn) interesting-tx)

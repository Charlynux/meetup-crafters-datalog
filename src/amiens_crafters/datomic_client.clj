(ns amiens-crafters.datomic-client
  (:require [datomic.client.api :as d]))

(def client (d/client {:server-type :dev-local
                       :system "dev"}))
;; or
(def client (d/client {:server-type :peer-server
                       :access-key "myaccesskey"
                       :secret "mysecret"
                       :endpoint "localhost:8998"
                       :validate-hostnames false}))

(def db-config {:db-name "meetup-crafters-demo"})

(comment
  (d/delete-database client db-config))
(d/create-database client db-config)

(def conn (d/connect client db-config))

;; Fail ! "Insufficient bindings, will cause db scan"
(d/q '[:find ?attr
       :where
       [_ ?attr _]]
     (d/db conn))

(d/q '[:find ?attr
       :where
       [_ :db/ident ?attr]]
     (d/db conn))

(def schema [;; Company
             {:db/ident :company/name
              :db/valueType :db.type/string
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one}
             ;; Employee
             {:db/ident :employee/id
              :db/valueType :db.type/string ;; :db.type/uuid
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one}
             {:db/ident :employee/name
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident :employee/company
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one}
             {:db/ident :employee/skills
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/many}])

(d/transact conn {:tx-data schema})

(def employees [{:employee/id "c58cfe40-73cd-4b9a-9c55-2e3587cc87cb"
                 :employee/name "Louis"
                 :employee/company {:company/name "Iteracode"}
                 :employee/skills #{"PHP" "CakePHP" "Symfony" "Javascript" "Ansible"}}
                {:employee/id "4c9a6eb4-7782-4361-a9a7-479ad26b7f1d"
                 :employee/name "Florent"
                 :employee/company {:company/name "Iteracode"}
                 :employee/skills #{"PHP" "CakePHP" "Wordpress" "Javascript" "Design"}}
                {:employee/id "c7ac8fc2-2fe5-4dab-acc2-382102ed0ef6"
                 :employee/name "Maxime"
                 :employee/company {:company/name "Iteracode"}
                 :employee/skills #{"PHP" "CakePHP" "Prestashop" "Javascript" "Talend"}}])

(d/transact conn {:tx-data employees})

;; Predicates
(d/q '[:find ?name
       :where
       [_ :employee/name ?name]
       [(.startsWith ?name "Flo")]]
     (d/db conn))

(defn useless-predicate [name]
  (= "Florent" name))

(d/q '[:find ?name
       :where
       [_ :employee/name ?name]
       [(amiens-crafters.datomic/useless-predicate ?name)]]
     (d/db conn))

(d/transact conn {:tx-data [{:company/name "Iteracode" :company/website "https://iteracode.fr"}]})

(d/transact conn {:tx-data [{:db/ident :company/website
                             :db/valueType :db.type/string
                             :db/cardinality :db.cardinality/one}]})

(d/transact conn {:tx-data [{:company/name "Iteracode" :company/website "https://iteracode.fr"}]})

(d/q '[:find ?attr
       :where
       [?e :employee/id _]
       [?e ?a _]
       [?a :db/ident ?attr]]
     (d/db conn))

(d/transact conn {:tx-data [{:db/ident :employee/role
                             :db/valueType :db.type/string
                             :db/cardinality :db.cardinality/one}
                            {:db/ident :import/file-name
                             :db/valueType :db.type/string
                             :db/cardinality :db.cardinality/one}]})

(d/transact conn {:tx-data
                  [{:db/id "datomic.tx" :import/file-name "20181122-iteracode-roles.csv"}
                   {:employee/id "c58cfe40-73cd-4b9a-9c55-2e3587cc87cb"
                    :employee/role "Bottom Dev"}
                   {:employee/id "4c9a6eb4-7782-4361-a9a7-479ad26b7f1d"
                    :employee/role "Bottom Dev"}
                   {:employee/id "c7ac8fc2-2fe5-4dab-acc2-382102ed0ef6"
                    :employee/role "Middle Dev"}]})

(d/transact conn {:tx-data
                  [{:db/id "datomic.tx" :import/file-name "20201122-iteracode-roles.csv"}
                   {:employee/id "c58cfe40-73cd-4b9a-9c55-2e3587cc87cb"
                    :employee/role "Middle Dev"}
                   {:employee/id "4c9a6eb4-7782-4361-a9a7-479ad26b7f1d"
                    :employee/role "Middle Dev"}
                   {:employee/id "c7ac8fc2-2fe5-4dab-acc2-382102ed0ef6"
                    :employee/role "Middle Dev"}]})

(d/q '[:find ?name ?role ?source
       :where
       [?e :employee/name ?name]
       [?e :employee/role ?role ?tx]
       [?tx :import/file-name ?source]]
     (d/db conn))

(d/q '[:find ?name ?role ?tx
       :where
       [?e :employee/name ?name]
       [?e :employee/role ?role ?tx]]
     (d/history (d/db conn)))

;; Définition d'un Datom
;; [entity attribute value transaction op?]
(d/q '[:find ?name ?role ?tx ?op
       :where
       [?e :employee/name ?name]
       [?e :employee/role ?role ?tx ?op]]
     (d/history (d/db conn)))

(def first-import-tx
  (ffirst (d/q '[:find ?tx
                 :where [?tx :import/file-name "20181122-iteracode-roles.csv"]]
               (d/db conn))))

;; d/as-of

(d/q '[:find ?name ?role
       :where
       [?e :employee/name ?name]
       [?e :employee/role ?role]]
     (d/as-of (d/db conn) first-import-tx))

;; d/since

(d/q '[:find ?name ?role
       :where
       [?e :employee/name ?name]
       [?e :employee/role ?role]]
     (d/since (d/db conn) first-import-tx))
;; Retourne [] puisque depuis le deuxième import, il n'y a pas eu d'ajout de noms d'employés

(d/q '[:find ?role
       :where
       [_ :employee/role ?role]]
     (d/since (d/db conn) first-import-tx))

(d/q '[:find ?name ?role
       :in $db $recent
       :where
       [$db ?e :employee/name ?name]
       [$recent ?e :employee/role ?role]]
     (d/db conn)
     (d/since (d/db conn) first-import-tx))

;;;;;;
;; Requêtes autour des transactions
;;;;;

;; Quand l'information "role" a-t-elle été ajoutée ?
(d/q '[:find ?tx ?inst
       :where
       [_ :db/ident :employee/role ?tx]
       [?tx :db/txInstant ?inst]] (d/db conn))
;; => [[13194139533322 #inst "2020-12-04T18:12:33.732-00:00"]]

;; Quand a-t-elle été utilisée la première fois ?
(d/q '[:find (min ?tx)
       :where
       [_ :employee/role _ ?tx]]
     (d/db conn))
;; => [[13194139533323]]

(def interesting-tx (ffirst *1))

;; Quelles sont toutes les valeurs modifiées par cette transaction ?
(d/q '[:find ?attr ?value
       :where
       [_  ?a ?value ?tx]
       [?a :db/ident ?attr]]
     (d/db conn)
     interesting-tx)
;; Erreur : Insufficent bindings, will cause db scan
;; Pas d'index sur la transaction

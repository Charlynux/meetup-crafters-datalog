(ns amiens-crafters.datahike
  (:require [datahike.api :as d]))

(def cfg {:store {:backend :mem}})

(d/create-database cfg)

(def conn (d/connect cfg))

(d/transact conn [{:db/ident :name
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one }
                  {:db/ident :age
                   :db/valueType :db.type/long
                   :db/cardinality :db.cardinality/one }])

(d/transact conn
             [{:name "Louis" :age 12}
              {:name "Charles" :age 38}])

;; a first simple query
(d/q '[:find ?name
       :where [_ :name ?name]]
     (d/db conn))

;; Dans un Datom, TOUT peut-être requêté !!!
(d/q '[:find ?attr
       :where
       [_ ?attr _]]
     (d/db conn))

(assert (= @conn
           (d/db conn)))

(d/q '[:find ?name ?age
       :where
       [?e :name ?name]
       [?e :age ?age]]
     @conn)

;; Identifiant de l'entité "Louis"
(def id-louis (first (first (d/q '[:find ?id
                                   :where
                                   [?id :name "Louis"]]
                                 @conn))))

(def db-before-update @conn)

;; Mise à jour de l'âge de "Louis"
(d/transact conn
             [{:db/id id-louis :name "Louis" :age 26}])

;; Mise à jour effectuée ?
(d/q '[:find ?name ?age
       :where
       [?e :name ?name]
       [?e :age ?age]]
     @conn)

;; Database is immutable
(d/q '[:find ?name ?age
       :where
       [?e :name ?name]
       [?e :age ?age]]
     db-before-update)

;; "Quand" chaque attribut de Louis a été mis à jour ?

;; Définition (presque) complète d'un Datom
;; [entity attribute value transaction]

(d/q '[:find ?attr ?value ?tx
       :where
       [?e :name "Louis"]
       [?e ?attr ?value ?tx]]
     @conn)

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

(d/transact conn schema)

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

(d/transact conn employees)

;; Juste pour vérifier que les données sont bien chargées
(comment
  (d/q '[:find ?name
         :where [_ :employee/name ?name]]
       @conn)
  (d/q '[:find ?e ?name
         :where [?e :company/name ?name]]
       @conn))

;; Génère une erreur puisque le schéma ne comprend pas ":company/website"
(d/transact conn [{:company/name "Iteracode" :company/website "https://iteracode.fr"}])

(d/transact conn [{:db/ident :company/website
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}])

(d/transact conn [{:company/name "Iteracode" :company/website "https://iteracode.fr"}])

(d/q '[:find ?e ?name ?website
       :where
       [?e :company/name ?name]
       [?e :company/website ?website]]
     @conn)

(d/q '[:find ?name
       :where
       [?e :employee/name ?name]
       [?e :employee/skills "PHP"]]
     @conn)

;; Utiliser un paramètre
(def query '[:find ?name
             :in $ ?skill
             :where
             [?e :employee/name ?name]
             [?e :employee/skills ?skill]])

(d/q query @conn "Symfony")
(d/q query @conn "Talend")

;; Tuples
(d/q '[:find ?name
       :in $ [?language ?tech]
       :where
       [?e :employee/name ?name]
       [?e :employee/skills ?language]
       [?e :employee/skills ?tech]]
     @conn ["PHP" "Ansible"])

;; Collections
(d/q '[:find ?name
       :in $ [?skill ...]
       :where
       [?e :employee/name ?name]
       [?e :employee/skills ?skill]]
     @conn ["Symfony" "Wordpress"])

;; Relations
(d/q '[:find ?skill
       :in $ [[?name ?role]]
       :where
       [?e :employee/name ?name]
       [?e :employee/skills ?skill]
       [(= ?role "Bottom Dev")]]
     @conn
     [["Louis" "Bottom Dev"]
      ["Florent" "Bottom Dev"]
      ["Maxime" "Middle Dev"]])

(def at-least-2-rule
  '[(at-least-2 ?skill)
    [?e1 :employee/skills ?skill]
    [?e2 :employee/skills ?skill]
    [(not= ?e1 ?e2)]])

(d/q '[:find ?skill
       :in $ %
       :where
       (at-least-2 ?skill)]
     @conn [at-least-2-rule])

;; Recursive rule
(d/q '[:find ?e
       :in $ %
       :where
       [?e :gender :male]
       (ascendant? ?e 7)]
     [[1 :parent 3]
      [2 :parent 3]
      [3 :parent 5]
      [4 :parent 5]
      [5 :parent 7]
      [6 :parent 7]
      [1 :gender :male]
      [3 :gender :male]
      [5 :gender :male]
      [7 :gender :male]]
     '[[(ascendant? ?e1 ?e2)
        [?e1 :parent ?e2]]
       [(ascendant? ?e1 ?e2)
        [?e1 :parent ?e3]
        (ascendant? ?e3 ?e2)]])

;; Aggregates
(d/q '[:find (count ?name)
       :where
       [_ :employee/name ?name]]
     @conn)

(d/q '[:find ?name (count ?skill)
       :where
       [?e :employee/name ?name]
       [?e :employee/skills ?skill]]
     @conn)

;; Use your own predicates
(d/q '[:find ?name
       :in $ predicate
       :where
       [?e :employee/name ?name]
       [?e :employee/skills ?skill]
       [(predicate ?skill)]]
     @conn
     (fn [skill] (.endsWith skill "PHP")))

;; Requêter plusieurs bases de données
(d/q '[:find ?name
       :in $skills $ages
       :where
       [$skills ?e-skill :employee/name ?name]
       [$skills ?e-skill :employee/skills "PHP"]

       [$ages ?e-name :name ?name]
       [$ages ?e-name :age ?age]
       [(< ?age 35)]]
     @conn
     db-before-update)

;; 'Pull' Queries

(d/pull
 @conn
 '[:employee/name
   {:employee/company [:company/name]}
   :employee/skills]
 [:employee/id "c58cfe40-73cd-4b9a-9c55-2e3587cc87cb"])

(d/q '[:find [(pull ?e
                    [:employee/name
                      {:employee/company [:company/name]}
                     :employee/skills])
              ...] ;; Pull all values
       :where
       [?e :employee/name ?name]
       [?e :employee/skills "PHP"]]
     @conn)

;; Filtered DB
(def anonymized-db
  (d/filter
   @conn
   (fn [_ [?e ?a ?v ?tx]]
     (not
      (or (= ?a :employee/company)
          (= ?a :employee/name))))))

(d/q '[:find ?id
       :where
       [?e :employee/id ?id]
       [?e :employee/skills "Talend"]]
     anonymized-db)

;; Autres usages
;; - Sécurité : filter "owns to user"
;; - Exclusion de données erronées (cf. 'reified transactions' dans Datomic)

;; Visitons le futur
(def whatif-db
  (d/db-with
   @conn
   [{:employee/id "4c9a6eb4-7782-4361-a9a7-479ad26b7f1d" :employee/skills #{"React"}}]))

(d/q '[:find ?name (count ?skill)
       :where
       [?e :employee/name ?name]
       [?e :employee/skills ?skill]]
     whatif-db)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reprise des requêtes Datomic
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(d/q '[:find ?e ?role
       :where
       [?e :employee/role ?role]]
     (d/since (d/db conn) first-import-tx))

;; Ici, on utilise deux databases.
;; - conn pour récupérer les employés actuels
;; - (d/since ...) pour n'avoir que les modifications récentes
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
(d/q '[:find ?a ?value
       :in $ ?tx
       :where
       [_  ?a ?value ?tx]]
     (d/db conn)
     interesting-tx)

(require '[datascript.core :as d])

(def conn-schemaless (d/create-conn))

(d/transact! conn-schemaless
             [{:name "Louis" :age 12}
              {:name "Charles" :age 38}])

;; a first simple query
(d/q '[:find ?name
       :where [_ :name ?name]]
     (d/db conn-schemaless))

;; Datoms partial definition
;; [entity attribute value]
(def sort-of-db [[1 :name "Louis"]
                 [1 :age 12]
                 [2 :name "Charles"]
                 [2 :age 38]])

(d/q '[:find ?name
       :where
       [_ :name ?name]]
     sort-of-db)

;; Dans un Datom, TOUT peut-être requêté !!!
(d/q '[:find ?attr
       :where
       [_ ?attr _]]
     (d/db conn-schemaless))

(assert (= @conn-schemaless
           (d/db conn-schemaless)))

(d/q '[:find ?name ?age
       :where
       [?e :name ?name]
       [?e :age ?age]]
     @conn-schemaless)

;; Identifiant de l'entité "Louis"
(def id-louis (first (first (d/q '[:find ?id
                                   :where
                                   [?id :name "Louis"]]
                                 @conn-schemaless))))

(def db-before-update @conn-schemaless)

;; Mise à jour de l'âge de "Louis"
(d/transact! conn-schemaless
             [{:db/id id-louis :name "Louis" :age 26}])

;; Mise à jour effectuée ?
(d/q '[:find ?name ?age
       :where
       [?e :name ?name]
       [?e :age ?age]]
     @conn-schemaless)

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
     @conn-schemaless)

(def schema {:company/name
             {:db/unique :db.unique/identity}
             :employee/id
             {:db/unique :db.unique/identity}
             :employee/company
             {:db/valueType :db.type/ref}
             :employee/skills
             {:db/cardinality :db.cardinality/many}})

(def conn (d/create-conn schema))

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

(d/transact! conn employees)

;; Juste pour vérifier que les données sont bien chargées
(comment
  (d/q '[:find ?name
         :where [_ :employee/name ?name]]
       @conn)
  (d/q '[:find ?e ?name
         :where [?e :company/name ?name]]
       @conn))

;; :db/unique :db.unique/identity : Identifiant d'une entité

(d/transact! conn [{:company/name "Iteracode" :company/website "https://iteracode.fr"}])

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

;; TODO
;; Queries
;;  Multiple DBs
;;  Pull + (d/q (pull))
;; Filtered DB
;; db/with


(d/q '[:find ?name
       :in $ %
       :where
       [?e :name ?name]
       [?e :age ?age]
       (old? ?age)]
     @conn-schemaless
     '[[(old? ?age)
        [(> ?age 30)]]])



;; Database filter
(d/filter
 @conn-schemaless
 (fn [_ [?e ?a ?v ?tx]]
   (not= ?a :age)))

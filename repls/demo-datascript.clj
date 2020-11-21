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
(d/q '[:find ?attr ?value ?tx
       :where
       [?e :name "Louis"]
       [?e ?attr ?value ?tx]]
     @conn-schemaless)

;; TODO
;; Schema with sub-entities
;; Queries
;;  Params ($)
;;  Param "Collections"
;;  Param "Relations"
;;  Multiple DBs
;;  Rules
;;  Pull + (d/q (pull))
;;  Aggregates
;;  Predicates + Your own
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

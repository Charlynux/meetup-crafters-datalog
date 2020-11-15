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

(assert (= @conn-schemaless
           (d/db conn-schemaless)))

(d/q '[:find ?name ?age
       :where
       [?e :name ?name]
       [?e :age ?age]]
     @conn-schemaless)

;; TOUT peut-être requêté !!!
(d/q '[:find ?attr
       :where
       [_ ?attr _]]
     @conn-schemaless)

(def id-louis (first (first (d/q '[:find ?id
                                   :where
                                   [?id :name "Louis"]]
                                 @conn-schemaless))))

(d/transact! conn-schemaless
             [{:db/id id-louis :name "Louis" :age 26}])

#_(d/transact! conn-schemaless
             [{:db/id id-louis :role "Middle Dev"}]
             {:iteracode/imported-file "20201111-employees-export.csv"})

#_(d/q '[:find ?attr ?tx ?txInstant
       :where
       [?e :name "Louis"]
       [?e ?attr ?value ?tx]
       #_[?tx :iteracode/imported-file ?source]
       [?tx :db/txInstant ?txInstant]]
     @conn-schemaless)

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

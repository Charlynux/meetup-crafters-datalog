(ns amiens-crafters.datomic
  (:require [datomic.client.api :as d]))

(def client (d/client {:server-type :dev-local
                       :system "dev"}))

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

;; TODO
;; d/since
;; d/as-of
;; d/history eavt op?

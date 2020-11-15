(ns amiens-crafters.datomic
  (:require [datomic.client.api :as d]))

(def client (d/client {:server-type :dev-local
                       :system "dev"}))

(def db-config {:db-name "movies"})

(d/create-database client db-config)

(def conn (d/connect client db-config))

(def movie-schema [{:db/ident :movie/title
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "The title of the movie"
                    :db/unique :db.unique/identity}

                   {:db/ident :movie/genre
                    :db/valueType :db.type/string
                    :db/cardinality :db.cardinality/one
                    :db/doc "The genre of the movie"}

                   {:db/ident :movie/release-year
                    :db/valueType :db.type/long
                    :db/cardinality :db.cardinality/one
                    :db/doc "The year the movie was released in theaters"}])

(d/transact conn {:tx-data movie-schema})

(def first-movies [{:movie/title "The Goonies"
                    :movie/genre "action/adventure"
                    :movie/release-year 1985}
                   {:movie/title "Commando"
                    :movie/genre "thriller/action"
                    :movie/release-year 1985}
                   {:movie/title "Repo Man"
                    :movie/genre "punk dystopia"
                    :movie/release-year 1984}])

(d/transact conn {:tx-data first-movies})
#_(d/transact conn first-movies)

(d/q '[:find ?attr
       :where
       ;[?e :movie/title _]
       [?e ?a _]
       [?a :db/ident ?attr]]
     (d/db conn))

(d/q '[:find ?year ?tx ?op
       :where [_ :movie/release-year ?year ?tx ?op]]
     (d/history (d/db conn)))

(d/transact conn {:tx-data [{:movie/title "The Goonies"
                            :movie/genre "action/adventure"
                            :movie/release-year 1986}
                           {:movie/title "Commando"
                            :movie/genre "thriller/action"
                            :movie/release-year 1985}
                           {:movie/title "Repo Man"
                            :movie/genre "punk dystopia"
                            :movie/release-year 1981}]})

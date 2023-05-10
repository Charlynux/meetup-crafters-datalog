(ns amiens-crafters.xtdb
  "L'objectif de cette session de REPL n'est pas d'explorer XTDB en tant que tel.
  On cherchera à comparer aux sessions réalisées avec Datascript et Datomic.

  J'ai ouvert demo-datascript.clj et converti le code à la sauce XTDB, en découvrant l'API au fur et à mesure."
  (:require [xtdb.api :as xt]))

(def xtdb
  "Sans option, le noeud est 'in-memory'."
  (xt/start-node {}))

(xt/submit-tx xtdb [[::xt/put {:xt/id :louis :name "Louis" :age 12}]
                      [::xt/put {:xt/id :charles :name "Charles" :age 38}]])

(xt/q (xt/db xtdb)
        '{:find [name]
          :where [[_ :name name]]})

(def requete-age-et-nom
  "Requête pour rechercher l'age et le nom de tout le monde"
  '{:find [name age]
    :where [[e :name name]
            [e :age age]]})

(xt/q (xt/db xtdb) requete-age-et-nom)

(def db-before-update
  "On stocke la database avant la modification pour démontrer son immutabilité."
  (xt/db xtdb))

(xt/submit-tx xtdb [[::xt/put {:xt/id :louis :name "Louis" :age 26}]])

(xt/q (xt/db xtdb) requete-age-et-nom)

(xt/q db-before-update requete-age-et-nom)

(xt/entity-history (xt/db xtdb) :louis :asc
                     {:with-docs? true})
;; => [{:xtdb.tx/tx-time #inst "2021-08-07T17:16:24.071-00:00",
;;      :xtdb.tx/tx-id 0,
;;      :xtdb.db/valid-time #inst "2021-08-07T17:16:24.071-00:00",
;;      :xtdb.db/content-hash
;;      #xt/id "fcc63f493ac4abf9bf71fd08c18708228ca228b1",
;;      :xtdb.db/doc {:xt/id :louis, :name "Louis", :age 12}}
;;     {:xtdb.tx/tx-time #inst "2021-08-07T17:16:32.719-00:00",
;;      :xtdb.tx/tx-id 1,
;;      :xtdb.db/valid-time #inst "2021-08-07T17:16:32.719-00:00",
;;      :xtdb.db/content-hash
;;      #xt/id "a1937051b7fbfdbfa9533967f3572794c0b77cfc",
;;      :xtdb.db/doc {:xt/id :louis, :name "Louis", :age 26}}]

;; Avec l'option `withDoc`, on obtient le détail de la transaction.
;; A première vue, contrairement à Datomic, on obtient toutes les données soumises.
;; Dans la requête équivalente, Datomic retournait uniquement quand une valeur était apparue pour la première fois.
;; A noter que je n'ai pas creusé le sujet, il est peut-être possible d'avoir le même type de réponse avec XTDB.

;; Ici, nous devrions manipuler des données imbriquées. Mais mes premières lectures de documentation/tutoriels ne parlent que de
;; données "plates".
;; Je ne vais pas chercher plus avant pour l'instant et abandonner l'idée de "company" pour un employé.

;; Je reprends exactement les données de `demo-datascript.clj` et `demo-datomic.clj`.
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

(defn employee->document
  "Cette fonction fait les adaptations nécessaires avant d'envoyer la donnée dans XTDB."
  [employee]
  (-> employee
      (dissoc :employee/company)
      (assoc :xt/id (:employee/id employee))))

(xt/submit-tx xtdb
                (mapv
                 (fn [employee] [::xt/put (employee->document employee)
                                 #inst "2020-02-11"])
                 employees))

(xt/q
 (xt/db xtdb)
 '{:find [name]
   :where [[_ :employee/name name]]})

(xt/q
 (xt/db xtdb)
 '{:find [name]
   :where [[e :employee/name name]
           [e :employee/skills "PHP"]]})

(def skill-query
  '{:find [name]
    :where [[e :employee/name name]
            [e :employee/skills skill]]
    :in [skill]})

(xt/q
 (xt/db xtdb)
 skill-query
 "Symfony")

(xt/q
 (xt/db xtdb)
 skill-query
 "Talend")

(xt/q
 (xt/db xtdb)
 '{:find [name]
   :where [[e :employee/name name]
           [e :employee/skills language]
           [e :employee/skills tech]]
   :in [[language tech]]}
 ["PHP" "Ansible"])

(xt/q
 (xt/db xtdb)
 '{:find [name]
   :where [[e :employee/name name]
           [e :employee/skills skill]]
   :in [[skill ...]]}
 ["Symfony" "Wordpress"])

(xt/q
 (xt/db xtdb)
 '{:find [skill]
   :where [(at-least-2 skill)]
   :rules [[(at-least-2 ?skill)
            [?e1 :employee/skills ?skill]
            [?e2 :employee/skills ?skill]
            [(not= ?e1 ?e2)]]]})

(xt/q
 (xt/db xtdb)
 '{:find [(count name)]
   :where [[_ :employee/name name]]})

(xt/q
 (xt/db xtdb)
 '{:find [name (count skill)]
   :where [[e :employee/name name]
           [e :employee/skills skill]]})


;; Equivalent pour `d/as-of`
(def updated-maxime {:employee/id "c7ac8fc2-2fe5-4dab-acc2-382102ed0ef6"
                     :employee/name "Maxime"
                     :employee/company {:company/name "Iteracode"}
                     :employee/skills #{"PHP" "CakePHP" "Prestashop" "Javascript" "Talend" "Accounting"}})
(xt/submit-tx xtdb
                [[::xt/put
                  (employee->document updated-maxime)
                  #inst "2021-03-07"]])

(defn find-accounters [db]
  (xt/q
   db
   '{:find [name]
     :where [[e :employee/name name]
             [e :employee/skills skill]]
     :in [[skill ...]]}
   ["Accounting"]))

(find-accounters (xt/db xtdb #inst "2020-02-19"))

(find-accounters (xt/db xtdb))


;; Pour arrêter le noeud
(.close xtdb)

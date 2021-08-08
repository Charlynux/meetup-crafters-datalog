(ns amiens-crafters.crux
  "L'objectif de cette session de REPL n'est pas d'explorer Crux en tant que tel.
  On cherchera à comparer aux sessions réalisées avec Datascript et Datomic.

  J'ai ouvert demo-datascript.clj et converti le code à la sauce Crux, en découvrant l'API au fur et à mesure."
  (:require [crux.api :as crux]))

(def crux
  "Sans option, le noeud est 'in-memory'."
  (crux/start-node {}))

(crux/submit-tx crux [[:crux.tx/put {:crux.db/id :louis :name "Louis" :age 12}]
                      [:crux.tx/put {:crux.db/id :charles :name "Charles" :age 38}]])

(crux/q (crux/db crux)
        '{:find [name]
          :where [[_ :name name]]})

(def requete-age-et-nom
  "Requête pour rechercher l'age et le nom de tout le monde"
  '{:find [name age]
    :where [[e :name name]
            [e :age age]]})

(crux/q (crux/db crux) requete-age-et-nom)

(def db-before-update
  "On stocke la database avant la modification pour démontrer son immutabilité."
  (crux/db crux))

(crux/submit-tx crux [[:crux.tx/put {:crux.db/id :louis :name "Louis" :age 26}]])

(crux/q (crux/db crux) requete-age-et-nom)

(crux/q db-before-update requete-age-et-nom)

(crux/entity-history (crux/db crux) :louis :asc
                     {:with-docs? true})
;; => [{:crux.tx/tx-time #inst "2021-08-07T17:16:24.071-00:00",
;;      :crux.tx/tx-id 0,
;;      :crux.db/valid-time #inst "2021-08-07T17:16:24.071-00:00",
;;      :crux.db/content-hash
;;      #crux/id "fcc63f493ac4abf9bf71fd08c18708228ca228b1",
;;      :crux.db/doc {:crux.db/id :louis, :name "Louis", :age 12}}
;;     {:crux.tx/tx-time #inst "2021-08-07T17:16:32.719-00:00",
;;      :crux.tx/tx-id 1,
;;      :crux.db/valid-time #inst "2021-08-07T17:16:32.719-00:00",
;;      :crux.db/content-hash
;;      #crux/id "a1937051b7fbfdbfa9533967f3572794c0b77cfc",
;;      :crux.db/doc {:crux.db/id :louis, :name "Louis", :age 26}}]

;; Avec l'option `withDoc`, on obtient le détail de la transaction.
;; A première vue, contrairement à Datomic, on obtient toutes les données soumises.
;; Dans la requête équivalente, Datomic retournait uniquement quand une valeur était apparue pour la première fois.
;; A noter que je n'ai pas creusé le sujet, il est peut-être possible d'avoir le même type de réponse avec Crux.

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
  "Cette fonction fait les adaptations nécessaires avant d'envoyer la donnée dans Crux."
  [employee]
  (-> employee
      (dissoc :employee/company)
      (assoc :crux.db/id (:employee/id employee))))

(crux/submit-tx crux
                (mapv
                 (fn [employee] [:crux.tx/put (employee->document employee)])
                 employees))

(crux/q
 (crux/db crux)
 '{:find [name]
   :where [[_ :employee/name name]]})

(crux/q
 (crux/db crux)
 '{:find [name]
   :where [[e :employee/name name]
           [e :employee/skills "PHP"]]})

(def skill-query
  '{:find [name]
    :where [[e :employee/name name]
            [e :employee/skills skill]]
    :in [skill]})

(crux/q
 (crux/db crux)
 skill-query
 "Symfony")

(crux/q
 (crux/db crux)
 skill-query
 "Talend")

(crux/q
 (crux/db crux)
 '{:find [name]
   :where [[e :employee/name name]
           [e :employee/skills language]
           [e :employee/skills tech]]
   :in [[language tech]]}
 ["PHP" "Ansible"])

(crux/q
 (crux/db crux)
 '{:find [name]
   :where [[e :employee/name name]
           [e :employee/skills skill]]
   :in [[skill ...]]}
 ["Symfony" "Wordpress"])

(crux/q
 (crux/db crux)
 '{:find [skill]
   :where [(at-least-2 skill)]
   :rules [[(at-least-2 ?skill)
            [?e1 :employee/skills ?skill]
            [?e2 :employee/skills ?skill]
            [(not= ?e1 ?e2)]]]})

(crux/q
 (crux/db crux)
 '{:find [(count name)]
   :where [[_ :employee/name name]]})

(crux/q
 (crux/db crux)
 '{:find [name (count skill)]
   :where [[e :employee/name name]
           [e :employee/skills skill]]})

;; Pour arrêter le noeud
(.close crux)

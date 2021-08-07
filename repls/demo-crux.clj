(require '[crux.api :as crux])

(def crux (crux/start-node {}))

(crux/submit-tx crux [[:crux.tx/put {:crux.db/id :louis :name "Louis" :age 12}]
                      [:crux.tx/put {:crux.db/id :charles :name "Charles" :age 38}]])

(crux/q (crux/db crux)
        '{:find [name]
          :where [[_ :name name]]})

(crux/q (crux/db crux)
        '{:find [name age]
          :where [[e :name name]
                  [e :age age]]})

(def db-before-update (crux/db crux))

(crux/submit-tx crux [[:crux.tx/put {:crux.db/id :louis :name "Louis" :age 26}]])

(crux/q (crux/db crux)
        '{:find [name age]
          :where [[e :name name]
                  [e :age age]]})

(crux/q db-before-update
        '{:find [name age]
          :where [[e :name name]
                  [e :age age]]})

(.close crux)

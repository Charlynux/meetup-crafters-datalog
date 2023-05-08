INSTALL_DIRECTORY := .install

DATOMIC_VERSION := 1.0.6726
DATOMIC_ARTIFACT := datomic-pro-$(DATOMIC_VERSION)

WORKING_DIRECTORY := $(INSTALL_DIRECTORY)/$(DATOMIC_ARTIFACT)

$(INSTALL_DIRECTORY):
	mkdir $(INSTALL_DIRECTORY)

$(INSTALL_DIRECTORY)/$(DATOMIC_ARTIFACT).zip:
dl_datomic_zip: $(INSTALL_DIRECTORY)
	curl https://datomic-pro-downloads.s3.amazonaws.com/$(DATOMIC_VERSION)/$(DATOMIC_ARTIFACT).zip -qO $(INSTALL_DIRECTORY)/$(DATOMIC_ARTIFACT).zip

$(WORKING_DIRECTORY):
unzip_datomic: $(INSTALL_DIRECTORY)/$(DATOMIC_ARTIFACT).zip
	unzip $(INSTALL_DIRECTORY)/$(DATOMIC_ARTIFACT).zip -d $(INSTALL_DIRECTORY)

$(WORKING_DIRECTORY)/transactor.properties: $(WORKING_DIRECTORY)
	cd $(WORKING_DIRECTORY); \
	cp config/samples/dev-transactor-template.properties transactor.properties

install_local: $(WORKING_DIRECTORY) $(WORKING_DIRECTORY)/transactor.properties
	@echo "Installation prête"

run_transactor: install_local
	cd $(WORKING_DIRECTORY); \
	bin/transactor -Ddatomic.printConnectionInfo=true transactor.properties

run_peer: install_local
	cd $(WORKING_DIRECTORY); \
	bin/run ../../repls/init-datomic-database.clj; \
	bin/run -m datomic.peer-server -h localhost -p 8998 -a myaccesskey,mysecret -d meetup-crafters-demo,datomic:dev://localhost:4334/meetup-crafters-demo

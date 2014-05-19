
############################################################
# Component groups
############################################################

.PHONY: all

all: \
    collections/collectionstest \
    common/logtest \
    common/randutiltest \
    xmlreader/xmlreadertest

############################################################
# Component dependencies
############################################################

collections : collections/objcollections collections/intcollections \
    collections/strcollections collections/strintcollections
collections/collectionstest : collections
collections/intcollections : collections/objcollections
collections/objcollections : common
collections/strcollections : collections/objcollections
collections/strintcollections : collections/strcollections \
    collections/intcollections
common/logtest : common
common/randutiltest : common
sax : streams
streams : common
xmlreader : sax collections
xmlreader/xmlreadertest : xmlreader

############################################################
# Generic component rule
############################################################

%:
	@echo --- Component: $@

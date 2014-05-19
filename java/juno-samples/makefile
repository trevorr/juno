# Determine JOVE_HOME and PLI4J_HOME
ifndef JOVE_HOME
    $(error Please set JOVE_HOME to point to the root of a Jove installation)
endif
ifndef PLI4J_HOME
    ifneq ($(wildcard $(JOVE_HOME)/native),)
        PLI4J_HOME := $(JOVE_HOME)/native
    else
        $(error Cannot find $(JOVE_HOME)/native)
    endif
endif

# Ensure VERA_HOME is set correctly
ifeq ($(wildcard $(VERA_HOME)/include),)
    $(error Please set VERA_HOME to point to the root of a Vera installation)
endif

# Ensure JUNO_HOME is set correctly
ifeq ($(wildcard $(JUNO_HOME)/bin),)
    $(error Please set JUNO_HOME to point to the root of a Juno installation)
endif

# include host.mak to get SEP variable and escape it if it's a semicolon
include $(PLI4J_HOME)/tools/util.mak
include $(PLI4J_HOME)/tools/host.mak

# include cdefs.mak to get DLL_(PREFIX|SUFFIX)
include $(PLI4J_HOME)/tools/cdefs.mak

# Windows uses semicolon delimited classpaths, but in cygwin, the semicolons
# need to be escaped
ifeq ($(SEP),;)
    SEP := \;
endif

ifeq ($(HOST_OS),win32)
    JUNO := $(JUNO_HOME)/bin/juno.bat
else
    JUNO := $(JUNO_HOME)/bin/juno
endif

# makefile for building the jove-samples
.PHONY: java clean clean-verilog change_simulator require_simulator

JOVE_PROJECTS := jove langschema langschema-java langschema-jove newisys-utils randsolver
ifneq ($(wildcard $(JOVE_HOME)/bin/jove.jar),)
    # binary distribution
    CLASSPATH_PREFIX := $(JOVE_HOME)/bin/
    CLASSPATH_SUFFIX := .jar
else
    # source distribution
    CLASSPATH_PREFIX := $(JOVE_HOME)/java/
    CLASSPATH_SUFFIX := /bin
endif
JOVE_CLASSPATH := $(subst $(SPACE),$(SEP),$(foreach proj,$(JOVE_PROJECTS),$(CLASSPATH_PREFIX)$(proj)$(CLASSPATH_SUFFIX)) bin)

JUNO_PROJECTS := juno-runtime
JUNO_CLASSPATH := $(subst $(SPACE),$(SEP),$(foreach proj,$(JUNO_PROJECTS),$(JUNO_HOME)/java/$(proj)/bin))

CLASSPATH := $(JOVE_CLASSPATH):$(JUNO_CLASSPATH)

SIMULATOR ?= vcs
SIMULATOR_FLAGS := +javaclasspath=$(CLASSPATH)

RTL_SAMPLES := floptest
BEH_SAMPLES := xmlreader
SAMPLES := $(BEH_SAMPLES) $(RTL_SAMPLES)


default:
	@echo Please choose from the following sample targets: $(SAMPLES)

xmlreader: compile-xmlreader
	java -cp $(CLASSPATH) com.newisys.behsim.BehavioralLauncher \
	juno.samples.xmlreader.xmlreader.xmlreadertest.XmlreadertestProgram \
	+filename=xmlreader/src/xmlreader/xmlreadertest/test.xml

floptest: compile-floptest change_simulator
	make -C $(PLI4J_HOME) SIMULATOR=$(SIMULATOR) pli4j
	$(SIMULATOR_PATH)/$(SIMULATOR) $(SIMULATOR_FLAGS) \
	+javaclass=juno.samples.floptest.FloptestProgram \
	+filename=floptest/floptest.dat \
	floptest/dut.v floptest/dut.test_top.v src/juno/samples/floptest/floptest_shell.v

require_simulator:
# require a simulator if the target includes an RTL example
    ifneq ($(filter $(RTL_SAMPLES),$(MAKECMDGOALS)),)
        ifeq ($(SIMULATOR),vcs)
            ifneq ($(VCS_HOME),)
                ifeq ($(wildcard $(VCS_HOME)/*),)
                    $(error Please set VCS_HOME to point to the root of the VCS directory)
                endif
            else
                $(error Please set VCS_HOME to point to the root of the VCS directory)
            endif

            PLI4J_TAB := $(PLI4J_HOME)/comp/pli4j/src/pli4j.tab
            PLI4J_STATIC_LIBS := pli4j jnicpp vpicpp
            PLI4J_STATIC_LIB_PATHS := $(foreach lib,$(PLI4J_STATIC_LIBS),$(PLI4J_HOME)/obj/$(lib)/lib$(lib).a)
            SIMULATOR_PATH := $(VCS_HOME)/bin
            SIMULATOR_FLAGS += +v2k +vpi -R -P $(PLI4J_TAB) $(PLI4J_STATIC_LIB_PATHS) -cc g++ -LDFLAGS -g -lpthread
        else
            ifeq ($(SIMULATOR),cver)
                ifneq ($(CVER_HOME),)
                    ifeq ($(wildcard $(CVER_HOME)/*),)
                        $(error Please set CVER_HOME to point to the root of the cver directory)
                    endif
                else
                    $(error Please set CVER_HOME to point to the root of the cver directory)
                endif

                PLI4J_LIB := $(PLI4J_HOME)/obj/pli4j/$(DLL_PREFIX)pli4j$(DLL_SUFFIX)
                SIMULATOR_PATH := $(CVER_HOME)/bin
                SIMULATOR_FLAGS += +change_port_type +loadvpi=$(PLI4J_LIB):pli4j_register_vpi_tasks
            else
                $(error Unsupported simulator: $(SIMULATOR))
            endif
        endif
    endif

change_simulator: require_simulator
	/bin/sh changesimulator.sh $(SIMULATOR) $(PLI4J_HOME)

xlat-floptest:
	@echo "Translating floptest (output in xlat-floptest.log)"
	@$(JUNO) -veraroot floptest -verafile floptest.vr -syspath $(VERA_HOME)/include \
	-javaroot src -javapkg juno.samples.floptest > xlat.floptest.log

xlat-xmlreader:
	@echo "Translating xmlreader (output in xlat-xmlreader.log)"
	@$(JUNO) -veraroot xmlreader -veracomp all -syspath $(VERA_HOME)/include \
	-veraschemadump xmlreader.schema -javaroot src \
	-javapkg juno.samples.xmlreader > xlat-xmlreader.log

compile-floptest: xlat-floptest
	@echo "Compiling floptest"
	@ant -q floptest

compile-xmlreader: xlat-xmlreader
	@echo "Compiling xmlreader"
	@ant -q xmlreader

clean-verilog:
	rm -rf simv* csrc

clean: clean-verilog
	ant clean
	rm -rf src *.log *.out xmlreader.schema
	make -C $(PLI4J_HOME) clean

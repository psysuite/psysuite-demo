package iit.uvip.psysuite.suite


import iit.uvip.psysuite.PsySuiteInstrumentedTestBasic
import org.junit.runner.RunWith
import org.junit.runners.Suite

// Runs all unit tests.
@RunWith(Suite::class)
@Suite.SuiteClasses(PsySuiteInstrumentedTestBasic::class)

class PsySuiteInstrumentedTestSuite

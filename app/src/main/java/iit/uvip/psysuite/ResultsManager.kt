package iit.uvip.psysuite

import android.app.Activity
import android.app.AlertDialog
import android.content.res.Resources
import android.os.Environment
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.utility.TestResult
import kotlinx.coroutines.*
import org.albaspazio.core.accessory.SingletonHolder
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.filesystem.createFolder
import org.albaspazio.core.mail.EMailAccount
import org.albaspazio.core.mail.Mail
import org.albaspazio.core.mail.MailIntent
import org.albaspazio.core.ui.show1MethodDialog
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert

// SINGLETON
class ResultsManager private constructor(private val activity: Activity) {

    companion object : SingletonHolder<ResultsManager, Activity>(::ResultsManager)


    private var sendResult:Boolean = false

    private val resources: Resources = activity.resources

    private val emailAccount: EMailAccount      = EMailAccount("uvip.apptester@gmail.com", "uvipapptester19", "uvip.apptester@gmail.com")
    private var emailRecipients:Array<String>   = arrayOf("uvip.apptester@gmail.com")

    private lateinit var mailJob: Job
    private var mailAD: AlertDialog? = null

//    fun createResultsFolder() {
//        //create PsySuite results folder in /Downloads
////        if() {
//            createFolder(activity, Environment.DIRECTORY_DOWNLOADS + "/psysuite_data")
////        }
//    }

    // verify whether send results. if result.res_files is not empty and yes and abort ask whether sending anyway or not
    fun onTestFinished(result: TestResult){

        // check whether test defined specific recipients. otherwise use the default one(s)
        val ci          = getCompanionObjectMethod(result.testClass, "getEmailRecipients")
        if(ci.first != null)    emailRecipients = ci.first?.call(ci.second) as Array<String>

        if(sendResult && result.res_files.isNotEmpty()){

            if(result.code == TestBasic.TEST_COMPLETED) sendResult(result)          // test concluded
            else                                        askWhetherSending(result)   // test aborted or block ended. ask whether anyway submit results
        }
        else{
            when(result.code){
                TestBasic.TEST_COMPLETED            -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_completed_success))
                TestBasic.TEST_ABORTED_DEL_RESULT,
                TestBasic.TEST_ABORTED_KEEP_RESULT,
                TestBasic.TEST_ABORTED_WITH_ERROR   -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_completed_abort))
                TestBasic.BLOCK_COMPLETED           -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_partially_completed))
            }
        }
    }

    private fun askWhetherSending(result: TestResult){
        show2ChoisesDialog(activity, resources.getString(R.string.warning), resources.getString(R.string.ask_send_results), resources.getString(R.string.yes), resources.getString(R.string.no),
            { /* pressed YES */ sendResult(result) },{})
    }

    private fun sendResult(result: TestResult) {
        mailJob = GlobalScope.launch {
            try {
//                MailIntent.composeEmail(activity, "iit.uvip.psysuite.provider", emailRecipients, result.mailsubject, result.mailbody, result.res_files)
                mailAD = withContext(Dispatchers.Main) {
                    return@withContext show1MethodDialog(activity, resources.getString(R.string.warning), resources.getString(R.string.sending_results), resources.getString(R.string.abort)){
                        // abort mail submission
                        mailJob.cancel()
                        mailAD?.dismiss()
                        mailAD = null
                    }
                }
                val res = doSendResult(result)
                mailAD?.dismiss()

                withContext(Dispatchers.Main) {
                    if (res) showAlert(
                        activity,
                        resources.getString(R.string.success),
                        resources.getString(R.string.results_sent)
                    )
                    else showAlert(
                        activity,
                        resources.getString(R.string.failure),
                        resources.getString(R.string.email_account_error)
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showAlert(activity, resources.getString(R.string.failure), resources.getString(R.string.email_generic_error, e.toString()))
                }
                mailAD?.dismiss()

                withContext(Dispatchers.Main) {
                    show2ChoisesDialog(activity, resources.getString(R.string.warning),
                        resources.getString(R.string.ask_send_results_intent), resources.getString(R.string.yes), resources.getString(R.string.no),
                        {
                            MailIntent.composeEmail(
                                activity,
                                "iit.uvip.psysuite.provider",
                                emailRecipients,
                                result.mailsubject,
                                result.mailbody,
                                result.res_files
                            )   // pressed YES
                        }, {})
                }
            }
        }
    }

    private suspend fun doSendResult(res: TestResult):Boolean = withContext(Dispatchers.IO) {
        val mail = Mail(emailAccount)
        return@withContext  mail.send(emailRecipients, res.mailsubject, res.mailbody, res.res_files)
    }
}
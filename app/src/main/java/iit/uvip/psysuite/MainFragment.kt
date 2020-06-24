package iit.uvip.psysuite

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import iit.uvip.psysuite.core.common.TestBasic
import iit.uvip.psysuite.core.common.TestResult
import iit.uvip.psysuite.core.common.subjects_dialog.SubjectBasicDialogFragment
import iit.uvip.psysuite.core.common.subjects_parcel.SubjectBasicParcel
import iit.uvip.psysuite.core.tests.temporalbinding.atb.SubjectATBDialogFragment
import iit.uvip.psysuite.core.tests.temporalbinding.atb.SubjectATBParcel
import iit.uvip.psysuite.core.tests.tid.SubjectTIDDialogFragment
import iit.uvip.psysuite.core.tests.tid.SubjectTIDParcel
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.albaspazio.core.accessory.showAlert
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.mail.EMailAccount
import org.albaspazio.core.mail.Mail


class MainFragment : BaseFragment(
    layout = R.layout.fragment_main,
    landscape = false,
    hideAndroidControls = false
)
{
    private lateinit var subject: SubjectBasicParcel
    override val LOG_TAG:String = MainFragment::class.java.simpleName

    private var sendResult:Boolean = true

    private val dataDir:String = Environment.DIRECTORY_DOWNLOADS

    private val emailAccount:EMailAccount = EMailAccount(
        "alberto.inuggi@gmail.com", "12qw!\"QW", "alberto.inuggi@gmail.com")

    companion object {
        @JvmStatic val TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE: Int    = 1
        @JvmStatic val TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE: Int   = 2
        @JvmStatic val TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE: Int    = 3
        @JvmStatic val TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE: Int    = 4
        @JvmStatic val TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE: Int    = 5
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<TestResult>(TestBasic.TEST_BUNDLE_RESULT_LABEL)?.observe(
            viewLifecycleOwner) { result ->
            onTestFinished(result)
        }
        // /storage/self/primary/Download/a_2020522102124.txt
//        val dir = Environment.getExternalStoragePublicDirectory(dataDir).absolutePath
//        onTestFinished(TestResult(TestBasic.TEST_COMPLETED, arrayListOf("$dir/a_2020522102124.txt")))
    }

    override fun onResume(){
        super.onResume()

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        bt_start_tid_test.setOnClickListener{
            showTIDSubjectDialog()
        }

        bt_start_atb_test.setOnClickListener {
            showATBSubjectDialog()
        }

        bt_start_atvb_test.setOnClickListener {
            showATVBSubjectDialog()
        }

        bt_start_bisection.setOnClickListener {
            showBISSubjectDialog()
        }

        bt_start_musicalmeter.setOnClickListener {
            showMMDSubjectDialog()
        }
    }

    //================================================================================================================
    // 1 - SHOW SUBJECT DATA INSERTION DIALOG
    //================================================================================================================
    private fun showATBSubjectDialog(){

        subject                     = SubjectATBParcel.loadSubject()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.testClass           = "iit.uvip.psysuite.core.tests.temporalbinding.atb.TestATB"

        val bundle  = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectATBDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showATVBSubjectDialog() {

        subject                 = SubjectATBParcel.loadSubject()
        subject.canRecordAudio  = (activity as MainActivity).haveAudioRecordPermission
        subject.testClass       = "iit.uvip.psysuite.core.tests.temporalbinding.atvb.TestATVB"

        debugStart()
//        val bundle = Bundle()
//        bundle.putParcelable("subject", subject)
//
//        val editNameDialogFragment = SubjectATBDialogFragment()
//        editNameDialogFragment.setTargetFragment(this,TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE)
//        editNameDialogFragment.arguments = bundle
//        editNameDialogFragment.setCancelable(false)
//        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun debugStart() {
        subject.label               = "a"
        subject.age                 = 1
        subject.gender              = 1
        subject.type                = TestBasic.TEST_ATVB_TIME_DOUBLESTIM2
        subject.writeJson(requireContext())
        subject.nextTrailModality   = TestBasic.TEST_NEXTTRIAL_ANSWER
        startTest(subject)
    }

    private fun showTIDSubjectDialog(){

        subject                     = SubjectTIDParcel.loadSubject()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.testClass           = "iit.uvip.psysuite.core.tests.tid.TestTID"

        (subject as SubjectTIDParcel).spinner_data_resource = R.array.tid_sessions_array
        (subject as SubjectTIDParcel).first_modality = 0

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectTIDDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showBISSubjectDialog(){

        subject                     = SubjectBasicParcel.loadSubject()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.testClass           = "iit.uvip.psysuite.core.tests.bis.TestBIS"

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }

    private fun showMMDSubjectDialog() {

        subject                     = SubjectBasicParcel.loadSubject()
        subject.canRecordAudio      = (activity as MainActivity).haveAudioRecordPermission
        subject.testClass           = "iit.uvip.psysuite.core.tests.mmd.TestMMD"

        val bundle = Bundle()
        bundle.putParcelable("subject", subject)

        val editNameDialogFragment = SubjectBasicDialogFragment()
        editNameDialogFragment.setTargetFragment(this, TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE)
        editNameDialogFragment.arguments = bundle
        editNameDialogFragment.setCancelable(false)
        editNameDialogFragment.show(parentFragmentManager, "Modifica Soggetto")
    }
    //================================================================================================================
    // 2 - CALLBACK FROM DATA INSERTION DIALOG CLOSE
    //================================================================================================================
    // subject info !
    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {

        if (data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectBasicParcel? == null)
            return

        when(requestCode){
            TARGET_FRAGMENT_ATB_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectATBParcel
                (subject as SubjectATBParcel).writeJson(requireContext())
            }

            TARGET_FRAGMENT_ATVB_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectATBParcel
                (subject as SubjectATBParcel).writeJson(requireContext())
            }

            TARGET_FRAGMENT_TID_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectTIDParcel
                (subject as SubjectTIDParcel).writeJson(requireContext())
            }

            TARGET_FRAGMENT_BIS_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectBasicParcel
                subject.writeJson(requireContext())
            }

            TARGET_FRAGMENT_MMD_SUBJECT_REQUEST_CODE -> {
                subject = data?.getParcelableExtra(SubjectBasicDialogFragment.EVENT_SUBJECT) as SubjectBasicParcel
                subject.writeJson(requireContext())
            }
        }
        startTest(subject)
    }

    private fun startTest(subj:SubjectBasicParcel){

        val bundle = Bundle()
        bundle.putParcelable(TestBasic.TESTINFO_BUNDLE_LABEL, subj)
        Navigation.findNavController(requireView()).navigate(R.id.action_mainFragment_to_testFragment, bundle)
    }

    private fun onTestFinished(result:TestResult){

        GlobalScope.launch{
            try {
                val res = sendResult(result)
                if(res) showAlert(activity, "Success", "Result succesfully sent")
                else    showAlert(activity, "Failure", "Authentication/setup problems")
            } catch(e: Exception) {
                showAlert(activity, "Failure", "The following error was found: $e")
            }
        }
    }

    suspend fun sendResult(res:TestResult):Boolean = withContext(Dispatchers.IO) {
        val mail: Mail = Mail(emailAccount)
        return@withContext  mail.send(arrayOf("uvip.apptester@gmail.com"),
                                    "test result",
                                    "result", res.res_files)
    }
}


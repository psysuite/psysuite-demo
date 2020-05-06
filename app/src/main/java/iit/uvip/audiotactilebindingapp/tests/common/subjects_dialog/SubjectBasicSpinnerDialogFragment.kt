package iit.uvip.audiotactilebindingapp.tests.common.subjects_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import iit.uvip.audiotactilebindingapp.R
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.SubjectBasicListParcel
import iit.uvip.audiotactilebindingapp.tests.common.subjects_parcel.SubjectBasicParcel
import kotlinx.android.synthetic.main.fragment_subject_info_basic_list.*

class SubjectBasicSpinnerDialogFragment: SubjectBasicDialogFragment()
{
    override val LOG_TAG:String                 = SubjectBasicSpinnerDialogFragment::class.java.simpleName
    private var subject:SubjectBasicListParcel? = null
    private var nSpinnerElements:Int            = 0

    companion object {
        fun newInstance(title: String): SubjectBasicSpinnerDialogFragment {
            val frag =
                SubjectBasicSpinnerDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.setArguments(args)

            return frag
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subject_info_basic_list, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subject = arguments?.getParcelable("subject")

        if(subject == null)
            return

        ArrayAdapter.createFromResource(requireContext(), (subject as SubjectBasicListParcel).spinner_data_resource, android.R.layout.simple_spinner_item)
        .also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            nSpinnerElements = adapter.count
        }

        labSpinner.text = subject!!.spinner_label

        val title       = requireArguments().getString("title", "Enter Name")
        dialog?.setTitle(title)

        if(subject != null) updateGUI(subject!!)
        else                clear()
    }


    override fun updateGUI(subj: SubjectBasicParcel){
        super.updateGUI(subj)
        spinner.setSelection((subj as SubjectBasicListParcel).spinner_sel)
    }

    override fun clear(){
        super.clear()
        if(nSpinnerElements > 0)
            spinner.setSelection(0)
    }

    override fun updateSubject(): SubjectBasicListParcel?{

        subject = super.updateSubject() as SubjectBasicListParcel
        subject!!.spinner_sel = spinner.selectedItemId.toInt()
        return subject
    }

}
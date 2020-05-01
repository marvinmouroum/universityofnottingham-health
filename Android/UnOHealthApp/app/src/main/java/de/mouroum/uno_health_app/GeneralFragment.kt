package de.mouroum.uno_health_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class GeneralFragment(var layout: Int):Fragment() {

    //2
    companion object {

        fun newInstance(id:Int): GeneralFragment {
            return GeneralFragment(id)
        }
    }

    var createdView:View? = null

    //3
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(this.layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.createdView = view
    }


}
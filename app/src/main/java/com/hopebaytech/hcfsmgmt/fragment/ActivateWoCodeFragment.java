package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;

import org.w3c.dom.Text;

import java.util.Locale;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class ActivateWoCodeFragment extends Fragment {

    private final String CLASSNAME = ActivateWoCodeFragment.class.getSimpleName();

    private Context mContext;

    public static ActivateWoCodeFragment newInstance() {
        return new ActivateWoCodeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activate_wo_activation_code_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView hintMsg = (TextView) view.findViewById(R.id.hint_msg);
        String hyperLink = "<a href=\"http://www.hopebaytech.com\">TeraClient</a>";
        Spanned hintMsgText = Html.fromHtml(String.format(Locale.getDefault(), getString(R.string.activate_with_code_msg), hyperLink));
        hintMsg.setMovementMethod(LinkMovementMethod.getInstance());
        hintMsg.setText(hintMsgText);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

}

package nova.typoapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import nova.typoapp.comment.CommentContent;

public class CommentActivity extends FragmentActivity
implements CommentFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);



    }

    @Override
    public void onListFragmentInteraction(CommentContent.DummyItem item) {

    }
}

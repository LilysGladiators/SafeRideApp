package layout.activity_Moderator

class xml {
}

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools"
android:layout_width="match_parent"
android:layout_height="match_parent"
tools:context=".com.example.saferide.ModeratorActivity">

<Button
android:id="@+id/buttonAddDriver"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="Add Driver"
android:layout_centerHorizontal="true"
android:layout_marginTop="50dp"/>

<Button
android:id="@+id/buttonRemoveDriver"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="Remove Driver"
android:layout_below="@id/buttonAddDriver"
android:layout_centerHorizontal="true"
android:layout_marginTop="20dp"/>

</RelativeLayout>

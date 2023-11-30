package appCore.UsbModule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.List;

import appCore.R;

public class UsbDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List mList;
    private LayoutInflater mInflater;
    private String currDeviceAddress;

    public UsbDeviceAdapter(Context context, List list, String currDeviceAddress) {
        this.mContext = context;
        this.mList = list;
        this.currDeviceAddress = currDeviceAddress;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView tvName;
        ImageView icon;
        TextView state;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.usb_device_item, null);
            holder = new ViewHolder();
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_bluetooth_device_name);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.state = (TextView) convertView.findViewById(R.id.bluetooth_item_conn_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String   usbName = (String) mList.get(position);
        System.out.println("打印机信息"+mList.get(position));


        holder.tvName.setText(usbName);
        holder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.printer));


        return convertView;
    }
}


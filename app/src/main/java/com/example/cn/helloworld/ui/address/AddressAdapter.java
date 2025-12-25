package com.example.cn.helloworld.ui.address;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.data.model.Address;
import com.example.cn.helloworld.utils.AddressUtils;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    public interface AddressActionListener {
        void onSelect(Address address);

        void onEdit(Address address);

        void onDelete(Address address);

        void onSetDefault(Address address);
    }

    private final List<Address> addresses;
    private final AddressActionListener listener;
    private boolean selectionMode;

    public AddressAdapter(List<Address> addresses, AddressActionListener listener) {
        this.addresses = addresses;
        this.listener = listener;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {
        final Address address = addresses.get(position);
        holder.nameView.setText(safeText(address.getContactName()));
        holder.phoneView.setText(safeText(address.getContactPhone()));
        holder.detailView.setText(AddressUtils.buildFullAddress(address));
        holder.defaultView.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);
        holder.setDefaultButton.setVisibility(address.isDefault() ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectionMode && listener != null) {
                    listener.onSelect(address);
                }
            }
        });
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEdit(address);
                }
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(address);
                }
            }
        });
        holder.setDefaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSetDefault(address);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return addresses == null ? 0 : addresses.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView phoneView;
        final TextView detailView;
        final TextView defaultView;
        final TextView editButton;
        final TextView deleteButton;
        final TextView setDefaultButton;

        AddressViewHolder(View itemView) {
            super(itemView);
            nameView = (TextView) itemView.findViewById(R.id.text_address_name);
            phoneView = (TextView) itemView.findViewById(R.id.text_address_phone);
            detailView = (TextView) itemView.findViewById(R.id.text_address_detail);
            defaultView = (TextView) itemView.findViewById(R.id.text_address_default);
            editButton = (TextView) itemView.findViewById(R.id.button_address_edit);
            deleteButton = (TextView) itemView.findViewById(R.id.button_address_delete);
            setDefaultButton = (TextView) itemView.findViewById(R.id.button_address_default);
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}

/**
 * Copyright 2017. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.nearby.message;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.nearby.messages.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageDatAdaptor extends RecyclerView.Adapter<MessageDatAdaptor.MessageItem> {

    private List<Message> messages;

    public MessageDatAdaptor() {
        messages = new ArrayList<>();
    }

    @Override
    public MessageItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_item_message, parent, false);
        return new MessageItem(v);
    }

    @Override
    public void onBindViewHolder(MessageItem holder, int position) {
        Message m = messages.get(position);
        holder.tvMessage.setText(m.getNamespace());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void add(Message message) {

        messages.add(message);
        notifyDataSetChanged();
    }

    public void remove(Message message) {
        messages.remove(message);
        notifyDataSetChanged();
    }

    public class MessageItem extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public MessageItem(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
        }
    }
}

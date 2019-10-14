using System;
using Unity.Notifications.Android;
using UnityEngine;

public class NotificationMangager : MonoBehaviour
{
    private string m_channelId = "【ここにチャンネル ID】";
    int identifier;

    private void Awake()
    {
        identifier = -1;
        // 通知用のチャンネルを作成する
        var c = new AndroidNotificationChannel
        {
            Id = m_channelId,
            Name = "【ここにチャンネル名】",
            Importance = Importance.High,
            Description = "【ここに説明文】",
        };
        AndroidNotificationCenter.RegisterNotificationChannel(c);
    }

    // ボタンが押されたら呼び出される関数
    public void SendNotification()
    {
        // 通知を送信する
        var n = new AndroidNotification
        {
            Title = "【ここにタイトル】",
            Text = "【ここにテキスト】",
            SmallIcon = "icon_0",
            LargeIcon = "icon_1",
            FireTime = DateTime.Now.AddSeconds(1), // 1 秒後に通知
        };

        //通知IDが初期値のときだけ通知を送る
        if (AndroidNotificationCenter.CheckScheduledNotificationStatus(identifier) == NotificationStatus.Unknown)
        {
            identifier = AndroidNotificationCenter.SendNotification(n, m_channelId);
        }
    }
}

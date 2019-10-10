using System;
using Unity.Notifications.Android;
using UnityEngine;

public class NotificationMangager : MonoBehaviour
{
    private string m_channelId = "【ここにチャンネル ID】";

    private void Awake()
    {
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
    public void OnClickButton()
    {
        // 通知を送信する
        var n = new AndroidNotification
        {
            Title = "【ここにタイトル】",
            Text = "【ここにテキスト】",
            SmallIcon = "icon_0",
            LargeIcon = "icon_1",
            FireTime = DateTime.Now.AddSeconds(10), // 10 秒後に通知
        };
        AndroidNotificationCenter.SendNotification(n, m_channelId);
    }
}

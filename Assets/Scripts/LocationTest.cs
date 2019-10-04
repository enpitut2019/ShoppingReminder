using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class LocationTest : MonoBehaviour
{
    [SerializeField] Text locationText;

    IEnumerator Start()
    {
        //端末で位置情報使用許可がおりていない
        if (!Input.location.isEnabledByUser)
        {
            locationText.text = "permission error";
            yield break;
        }
        //位置情報取得開始
        Input.location.Start();
        int maxWait = 20;
        //位置情報取得準備中
        while (Input.location.status == LocationServiceStatus.Initializing && maxWait > 0)
        {
            locationText.text = "searching...";
            yield return new WaitForSeconds(1);
            maxWait--;
        }
        //タイムアウト
        if (maxWait < 1)
        {
            locationText.text = "Timed out";
            print("Timed out");
            yield break;
        }
        //位置情報取得不可
        if (Input.location.status == LocationServiceStatus.Failed)
        {
            locationText.text = "Unable to determine device location";
            print("Unable to determine device location");
            yield break;
        }
        //位置情報取得完了
        else
        {
            locationText.text = "緯度: " + Input.location.lastData.latitude + "\n" + "経度: " + Input.location.lastData.longitude;
            //print("Location: " + Input.location.lastData.latitude + " " + Input.location.lastData.longitude + " " + Input.location.lastData.altitude + " " + Input.location.lastData.horizontalAccuracy + " " + Input.location.lastData.timestamp);
        }
        //位置情報取得終了
        Input.location.Stop();
    }
}

using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class LocationManager : MonoBehaviour
{
    [SerializeField] Text locationText;
    int delay;

    IEnumerator Start()
    {
        delay = 0;
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
            ReadLocation();
        }
        //位置情報取得終了
        //Input.location.Stop();
    }

     void Update()
    {
        //位置情報取得可能かの判定
        if (Input.location.status == LocationServiceStatus.Running)
        {
            //位置情報を読み込む関数の遅延
            delay++;
            if (delay == 500)
            {
                ReadLocation();
                delay = 0;
            }
        }
    }

    void ReadLocation()
    {
        //位置情報を表示
        locationText.text = "緯度: " + Input.location.lastData.latitude + "\n" + "経度: " + Input.location.lastData.longitude;
    }

    //アプリが終了したときに呼び出される
    private void OnApplicationQuit()
    {
        //位置情報取得終了
        Input.location.Stop();
    }
}

using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class LocationTest : MonoBehaviour
{
    [SerializeField] Text testText;

    // Start is called before the first frame update
    void Start()
    {
        if (!Input.location.isEnabledByUser)
        {
            testText.text = "false";
            Debug.Log("false");
            return;
        }

        testText.text = "true";
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}

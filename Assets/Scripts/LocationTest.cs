using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LocationTest : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        if (!Input.location.isEnabledByUser)
        {
            Debug.Log("false");
            return;
        }
            
          
    }

    // Update is called once per frame
    void Update()
    {
        
    }
}

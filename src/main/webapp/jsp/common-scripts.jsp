<!-- common-scripts.jsp -->
<script>
    var environment = '<%= request.getAttribute("environment") != null ? request.getAttribute("environment") : "prod" %>';
    console.log("Current environment: " + environment);

    if (environment === 'dev') {
        var script = document.createElement('script');
        script.src = 'dev-tg.js';
        document.head.appendChild(script);
    } else {
        var script = document.createElement('script');
        script.src = 'prod-tg.js';
        document.head.appendChild(script);
    }

    var commonScript = document.createElement('script');
    commonScript.src = 'send-data.js';
    document.head.appendChild(commonScript);
</script>